package com.cps.mcp.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;
import com.embabel.agent.api.common.autonomy.Autonomy;
import com.embabel.agent.api.common.autonomy.AgentProcessExecution;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.core.ProcessOptions;
import com.embabel.agent.core.ActionInvocation;

@RestController
public class PlanController {

    private final Autonomy autonomy;

    public PlanController(Autonomy autonomy) {
        this.autonomy = autonomy;
    }

    @PostMapping("/plan")
    public Map<String, Object> plan(@RequestBody Map<String, Object> body) {
        String goalStr = (String) body.get("goal");
        if (goalStr == null || goalStr.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                "Goal is required"
            );
        }

        try {
            // Phase E: Classify intent for tracking/logging only
            // Planner will choose best matching goal based on action graph
            com.cps.mcp.agent.TravelIntent intent = classifyIntent(goalStr);
            System.out.println("PlanController: Classified intent=" + intent + " for goal: " + goalStr);
            
            return executeEmbabelPlanner(goalStr, intent);
        } catch (IllegalArgumentException ex) {
            System.err.println("PlanController: Validation error: " + ex.getMessage());
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                ex
            );
        } catch (Exception ex) {
            System.err.println("PlanController: Embabel execution failed: " + ex.getMessage());
            ex.printStackTrace();
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage(),
                ex
            );
        }
    }

    private com.cps.mcp.agent.TravelIntent classifyIntent(String goalStr) {
        if (goalStr == null) {
            return com.cps.mcp.agent.TravelIntent.SEARCH;
        }
        
        String normalized = goalStr.toLowerCase();
        
        // Weather intent keywords
        if (normalized.contains("weather") || normalized.contains("forecast") || 
            normalized.contains("rain") || normalized.contains("temperature") || 
            normalized.contains("sunny") || normalized.contains("climate") ||
            normalized.contains("snow") || normalized.contains("wind") ||
            normalized.contains("condition")) {
            return com.cps.mcp.agent.TravelIntent.WEATHER;
        }
        
        // Budget intent keywords
        if (normalized.contains("budget") || normalized.contains("cost") || 
            normalized.contains("price") || normalized.contains("expense") || 
            normalized.contains("estimate") || normalized.contains("how much") ||
            normalized.contains("spending") || normalized.contains("afford")) {
            return com.cps.mcp.agent.TravelIntent.BUDGET;
        }
        
        // Travel plan intent keywords
        if (normalized.contains("plan") || normalized.contains("trip") || 
            normalized.contains("vacation") || normalized.contains("itinerary") || 
            normalized.contains("weekend") || normalized.contains("holiday") ||
            normalized.contains("travel") || normalized.contains("create itinerary")) {
            return com.cps.mcp.agent.TravelIntent.TRAVEL_PLAN;
        }
        
        // Default to search for general information queries
        return com.cps.mcp.agent.TravelIntent.SEARCH;
    }

    private String mapIntentToGoal(com.cps.mcp.agent.TravelIntent intent) {
        switch (intent) {
            case WEATHER:
                return "Provide weather forecast";
            case BUDGET:
                return "Provide budget estimate";
            case SEARCH:
                return "Provide destination information";
            case TRAVEL_PLAN:
            default:
                return "Plan Travel Itinerary";
        }
    }

    private Map<String, Object> executeEmbabelPlanner(String goalStr, com.cps.mcp.agent.TravelIntent intent) throws Exception {
        // Pass original goal to Embabel - planner will choose best matching terminal goal
        // based on action preconditions, postconditions, and available execution paths
        AgentProcessExecution execution = autonomy.chooseAndRunAgent(goalStr, new ProcessOptions());
        AgentProcess process = execution.getAgentProcess();

        Class<?> agentClass = com.cps.mcp.agent.TravelPlannerAgent.class;
        List<ActionInvocation> history = process.getHistory();
        if (!history.isEmpty()) {
            String firstAction = history.get(0).getActionName();
            int lastDot = firstAction.lastIndexOf('.');
            if (lastDot != -1) {
                try {
                    agentClass = Class.forName(firstAction.substring(0, lastDot));
                } catch (Exception e) {
                    // ignore
                }
            }
        }

        List<Map<String, Object>> steps = com.cps.mcp.util.EmbabelTraceMapper.mapSteps(agentClass, process);
        List<Map<String, Object>> trace = com.cps.mcp.util.EmbabelTraceMapper.mapTrace(agentClass, process);
        String mermaidDiagram = com.cps.mcp.util.EmbabelGraphBuilder.generateMermaidDiagram(agentClass);

        String summary = "";
        Object finalReport = null;
        
        // Check for any of the possible result types
        for (Object obj : process.getObjects()) {
            String simpleClassName = obj.getClass().getSimpleName();
            if ("TravelPlanReport".equals(simpleClassName) || 
                "WeatherReportResult".equals(simpleClassName) ||
                "BudgetReportResult".equals(simpleClassName) ||
                "SearchReportResult".equals(simpleClassName)) {
                finalReport = obj;
                break;
            }
        }

        if (finalReport != null) {
            summary = com.cps.mcp.util.EmbabelTraceMapper.formatOutput(finalReport);
        } else {
            Object output = execution.getOutput();
            summary = output != null ? output.toString() : "";
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("goal", goalStr);
        response.put("classifiedIntent", intent.toString());
        response.put("classification", "embabel_runtime");
        response.put("status", "COMPLETED");
        response.put("steps", steps);
        response.put("trace", trace);
        response.put("mermaidDiagram", mermaidDiagram);
        response.put("summary", summary);
        response.put("source", "EMBABEL");

        return response;
    }
}