package com.cps.mcp.model;

import java.util.*;

public class BFSPlanningStrategy implements PlanningStrategy {

    @Override
    public PlanResult plan(State initialState, Goal goal, List<Action> actions) {
        class Node {
            final Set<String> state;
            final List<Action> path;

            Node(Set<String> state, List<Action> path) {
                this.state = state;
                this.path = path;
            }
        }

        Queue<Node> queue = new LinkedList<>();
        Set<Set<String>> visited = new HashSet<>();

        Set<String> startFacts = new HashSet<>(initialState.getFacts());
        queue.add(new Node(startFacts, new ArrayList<>()));
        visited.add(startFacts);

        Node bestNode = null;

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            if (isGoalSatisfied(current.state, goal)) {
                bestNode = current;
                break;
            }

            for (Action action : actions) {
                if (canExecute(current.state, action)) {
                    Set<String> nextState = applyEffects(current.state, action);
                    if (!visited.contains(nextState)) {
                        visited.add(nextState);
                        List<Action> nextPath = new ArrayList<>(current.path);
                        nextPath.add(action);
                        queue.add(new Node(nextState, nextPath));
                    }
                }
            }
        }

        List<Action> plan = new ArrayList<>();
        List<Map<String, Object>> trace = new ArrayList<>();

        if (bestNode != null) {
            plan = bestNode.path;
            Set<String> tempState = new HashSet<>(startFacts);

            for (Action action : plan) {
                List<String> stateBefore = new ArrayList<>(tempState);
                
                // Calculate missing preconditions
                List<String> missing = new ArrayList<>();
                for (String pre : action.getPreconditions()) {
                    if (!tempState.contains(pre)) {
                        missing.add(pre);
                    }
                }
                
                // Apply effects
                tempState.addAll(action.getEffects());
                List<String> stateAfter = new ArrayList<>(tempState);

                Map<String, Object> traceEntry = new LinkedHashMap<>();
                traceEntry.put("action", action.getName());
                traceEntry.put("state_before", stateBefore);
                traceEntry.put("preconditions_checked", action.getPreconditions());
                traceEntry.put("missing_preconditions", missing);
                traceEntry.put("effects_applied", action.getEffects());
                traceEntry.put("state_after", stateAfter);
                trace.add(traceEntry);
            }
        }

        return new PlanResult(plan, trace);
    }

    private boolean isGoalSatisfied(Set<String> state, Goal goal) {
        for (String condition : goal.getRequiredConditions()) {
            if (!state.contains(condition)) {
                return false;
            }
        }
        return true;
    }

    private boolean canExecute(Set<String> state, Action action) {
        for (String pre : action.getPreconditions()) {
            if (!state.contains(pre)) {
                return false;
            }
        }
        return true;
    }

    private Set<String> applyEffects(Set<String> state, Action action) {
        Set<String> next = new HashSet<>(state);
        next.addAll(action.getEffects());
        return next;
    }
}
