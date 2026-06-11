package com.cps.mcp.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;
import com.cps.mcp.model.*;
import com.cps.mcp.util.LLMService;
import com.cps.mcp.util.LLMServiceFactory;
import com.cps.mcp.util.MermaidVisualizer;

@RestController
public class PlanController {

    private final LLMServiceFactory llmServiceFactory;

    public PlanController(LLMServiceFactory llmServiceFactory) {
        this.llmServiceFactory = llmServiceFactory;
    }

    @PostMapping("/plan")
    public Map<String, Object> plan(@RequestBody Map<String, Object> body) {
        String goalStr = (String) body.get("goal");
        List<String> tools = (List<String>) body.get("tools");

        if (goalStr == null || goalStr.isBlank()) {
            Map<String, Object> errorRes = new LinkedHashMap<>();
            errorRes.put("error", "Goal is required");
            return errorRes;
        }

        // 1. Tool Logic: SearchAgent is ALWAYS included as the foundation agent
        List<String> allPossibleAgents = Arrays.asList("SearchAgent", "CalendarAgent", "BudgetAgent", "InviteAgent", "FoodAgent");
        List<String> userTools = (tools == null || tools.isEmpty()) ? allPossibleAgents : tools;
        
        // Ensure SearchAgent is always there even if not selected
        Set<String> toolSet = new LinkedHashSet<>(userTools);
        toolSet.add("SearchAgent"); 
        List<String> effectiveTools = new ArrayList<>(toolSet);

        // 2. Create initial state
        State state = new State();

        // 3. Resolve LLM provider from request body
        String provider = (String) body.get("provider");
        if (provider == null) {
            provider = "auto";
        }
        LLMService llmService = llmServiceFactory.getService(provider);

        // 4. Dynamic Action Generation using LLM (Tool-aware)
        String toolsStr = String.join(", ", effectiveTools);
        
        PlanningResponse planningResponse;
        try {
            planningResponse = llmService.generatePlan(goalStr, toolsStr);
        } catch (Exception e) {
            System.err.println("PlanController: LLM planning failed: " + e.getMessage());
            Map<String, Object> errorRes = new LinkedHashMap<>();
            errorRes.put("error", "Failed to generate plan: " + e.getMessage());
            return errorRes;
        }

        List<Action> allActions = new ArrayList<>();

        // 5. Convert DTO tasks into GOAP actions with validation/normalization
        List<PlanningTask> tasksList = planningResponse.getTasks();
        if (tasksList != null && !tasksList.isEmpty()) {
            for (int j = 0; j < tasksList.size(); j++) {
                PlanningTask task = tasksList.get(j);
                String name = task.getTitle();
                String desc = task.getDescription();
                String agent = task.getAgent();
                
                // Validate suggested agent against effectiveTools
                String validatedAgent = "SearchAgent";
                for (String t : effectiveTools) {
                    if (agent.toLowerCase().contains(t.toLowerCase().replace("agent", ""))) {
                        validatedAgent = t;
                        break;
                    }
                }
                
                // Extract preconditions and effects
                List<String> rawPre = task.getPreconditions();
                List<String> rawEff = task.getEffects();
                
                // Normalize and clean facts
                List<String> pre = new ArrayList<>();
                if (rawPre != null) {
                    for (String p : rawPre) {
                        if (p != null && !p.trim().isEmpty()) {
                            pre.add(p.trim().toLowerCase());
                        }
                    }
                }
                
                List<String> eff = new ArrayList<>();
                if (rawEff != null) {
                    for (String e : rawEff) {
                        if (e != null && !e.trim().isEmpty()) {
                            eff.add(e.trim().toLowerCase());
                        }
                    }
                }
                
                // Self-dependency detection & validation
                for (String p : pre) {
                    if (eff.contains(p)) {
                        Map<String, Object> errorRes = new LinkedHashMap<>();
                        errorRes.put("error", "Invalid plan: Self-dependency detected in task '" + name + "' (fact '" + p + "' is in both preconditions and effects).");
                        return errorRes;
                    }
                }
                
                // Deduplicate
                Set<String> preSet = new LinkedHashSet<>(pre);
                Set<String> effSet = new LinkedHashSet<>(eff);
                
                pre = new ArrayList<>(preSet);
                eff = new ArrayList<>(effSet);
                
                // Infer reasonable defaults if preconditions/effects are omitted
                if (pre.isEmpty()) {
                    pre = (j > 0) ? Arrays.asList("step_" + (j - 1) + "_done") : new ArrayList<>();
                }
                if (eff.isEmpty()) {
                    eff = Arrays.asList("step_" + j + "_done");
                }
                
                allActions.add(new Action("T" + task.getId(), name, desc, pre, eff, goalStr, validatedAgent));
            }
        } else {
            allActions.add(new Action("T1", "Complete general task", "No tasks found", new ArrayList<>(), Arrays.asList("done"), goalStr, effectiveTools.get(0)));
        }

        // 6. Goal Construction Priorities
        Goal goal = null;
        List<String> explicitGoalConditions = (List<String>) body.get("goal_conditions");
        if (explicitGoalConditions != null && !explicitGoalConditions.isEmpty()) {
            // Priority 1: Explicit goal_conditions
            List<String> normalizedGoal = new ArrayList<>();
            for (String g : explicitGoalConditions) {
                if (g != null && !g.trim().isEmpty()) normalizedGoal.add(g.trim().toLowerCase());
            }
            goal = new Goal(normalizedGoal);
            System.out.println("Goal constructed via Priority 1 (Explicit): " + normalizedGoal);
        } else {
            // Priority 2: Inferred semantic goal from the final task
            List<String> inferredSemantic = null;
            if (!allActions.isEmpty()) {
                Action finalAction = allActions.get(allActions.size() - 1);
                if (finalAction.getEffects() != null && !finalAction.getEffects().isEmpty()) {
                    inferredSemantic = finalAction.getEffects();
                }
            }
            
            if (inferredSemantic != null && !inferredSemantic.isEmpty()) {
                goal = new Goal(inferredSemantic);
                System.out.println("Goal constructed via Priority 2 (Semantic): " + inferredSemantic);
            } else {
                // Priority 3: Inferred leaf effects
                Set<String> allEffects = new HashSet<>();
                Set<String> allPreconditions = new HashSet<>();
                for (Action action : allActions) {
                    allEffects.addAll(action.getEffects());
                    allPreconditions.addAll(action.getPreconditions());
                }
                List<String> leafEffects = new ArrayList<>(allEffects);
                leafEffects.removeAll(allPreconditions);
                
                if (leafEffects.isEmpty() && !allActions.isEmpty()) {
                    leafEffects = allActions.get(allActions.size() - 1).getEffects();
                }
                goal = new Goal(leafEffects);
                System.out.println("Goal constructed via Priority 3 (Leaf Fallback): " + leafEffects);
            }
        }

        // 7. Validate plan inputs (self-loops, cyclic dependencies, unreachable goals, disconnected components)
        com.cps.mcp.util.PlanValidator.ValidationResult validationResult = com.cps.mcp.util.PlanValidator.validate(state, goal, allActions);
        if (!validationResult.isValid()) {
            Map<String, Object> errorRes = new LinkedHashMap<>();
            errorRes.put("error", validationResult.getErrorMessage());
            return errorRes;
        }

        // 8. Solve using GOAP Planner
        Planner planner = new Planner();
        PlanResult planResult = planner.plan(state, goal, allActions);
        List<Action> plan = planResult.getPlan();
        List<Map<String, Object>> trace = planResult.getTrace();

        // Validate plan outcome (detect cyclic dependency or unreachable goals)
        if (plan.isEmpty() && !allActions.isEmpty() && !goal.getRequiredConditions().isEmpty()) {
            Map<String, Object> errorRes = new LinkedHashMap<>();
            errorRes.put("error", "Failed to generate plan: Unreachable goal or cyclic dependency detected. GOAP could not find a path to satisfy " + goal.getRequiredConditions());
            return errorRes;
        }


        // 8. Dynamic Dependency Graph generation
        List<Map<String, Object>> tasks = new ArrayList<>();
        List<Map<String, Object>> executions = new ArrayList<>();
        
        // Map resolved Action list to task maps
        Map<Action, String> actionToId = new HashMap<>();
        int i = 1;
        for (Action action : plan) {
            String idStr = (action.getId() != null) ? action.getId() : "T" + i;
            actionToId.put(action, idStr);
            i++;
        }

        i = 1;
        for (Action action : plan) {
            Map<String, Object> task = new LinkedHashMap<>();
            String currentId = actionToId.get(action);
            task.put("id", currentId);
            task.put("description", action.getName() + " - " + action.getDescription());
            task.put("agent", action.getAgent());

            // Compute dynamic dependencies: find prior actions that satisfy preconditions
            List<String> deps = new ArrayList<>();
            for (int j = 0; j < i - 1; j++) {
                Action prevAction = plan.get(j);
                boolean hasOverlap = false;
                for (String effect : prevAction.getEffects()) {
                    if (action.getPreconditions().contains(effect)) {
                        hasOverlap = true;
                        break;
                    }
                }
                if (hasOverlap) {
                    deps.add(actionToId.get(prevAction));
                }
            }
            task.put("dependencies", deps);

            System.out.println("Executing " + task.get("id") + " with " + action.getAgent() + "...");
            String agentResponse;
            try {
                agentResponse = llmService.simulateAgentExecution(action.getAgent(), action.getName());
            } catch (Exception e) {
                System.err.println("PlanController: Agent simulation failed: " + e.getMessage());
                agentResponse = "Task completed by " + action.getAgent();
            }
            task.put("output", agentResponse);

            Map<String, Object> execResult = new LinkedHashMap<>();
            execResult.put("type", "MCP");
            execResult.put("agent", action.getAgent());
            execResult.put("task", action.getName());
            execResult.put("response", Map.of("content", agentResponse));
            executions.add(execResult);

            tasks.add(task);
            i++;
        }

        // 9. Visualizations
        String flowchart = MermaidVisualizer.generateFlowchart(tasks);
        String gantt = MermaidVisualizer.generateGantt(tasks);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("goal", goalStr);
        response.put("classification", "dynamic_plan");
        response.put("tasks", tasks);
        response.put("execution", executions);
        response.put("trace", trace);
        response.put("flowchart", flowchart);
        response.put("gantt", gantt);
        return response;
    }
}