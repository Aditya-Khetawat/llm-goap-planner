package com.cps.mcp.util;

import java.util.*;
import com.cps.mcp.model.*;

public class PlanValidator {

    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    public static ValidationResult validate(State initialState, Goal goal, List<Action> actions) {
        // 1. Self loop (self-dependency) check
        for (Action action : actions) {
            for (String pre : action.getPreconditions()) {
                if (action.getEffects().contains(pre)) {
                    return new ValidationResult(false, "Invalid plan: Self-dependency detected in task '" 
                            + action.getName() + "' (fact '" + pre + "' is in both preconditions and effects).");
                }
            }
        }

        // 2. Reachability analysis
        Set<String> reachableFacts = new HashSet<>(initialState.getFacts());
        Set<Action> reachableActions = new HashSet<>();
        boolean progress = true;
        while (progress) {
            progress = false;
            for (Action action : actions) {
                if (!reachableActions.contains(action)) {
                    boolean canExecute = true;
                    for (String pre : action.getPreconditions()) {
                        if (!reachableFacts.contains(pre)) {
                            canExecute = false;
                            break;
                        }
                    }
                    if (canExecute) {
                        reachableFacts.addAll(action.getEffects());
                        reachableActions.add(action);
                        progress = true;
                    }
                }
            }
        }

        // Check if all required goal conditions are satisfied
        List<String> missingGoalConditions = new ArrayList<>();
        for (String condition : goal.getRequiredConditions()) {
            if (!reachableFacts.contains(condition)) {
                missingGoalConditions.add(condition);
            }
        }

        if (!missingGoalConditions.isEmpty()) {
            // Check if missing goal conditions are produced by any action at all
            for (String missingCond : missingGoalConditions) {
                boolean producedByAny = false;
                for (Action action : actions) {
                    if (action.getEffects().contains(missingCond)) {
                        producedByAny = true;
                        break;
                    }
                }
                if (!producedByAny) {
                    return new ValidationResult(false, "Failed to generate plan: Unreachable goal condition. No available action produces '" + missingCond + "'.");
                }
            }

            // Group unreachable actions
            List<Action> unreachableActionsList = new ArrayList<>();
            for (Action action : actions) {
                if (!reachableActions.contains(action)) {
                    unreachableActionsList.add(action);
                }
            }

            // Find if there is a cycle among unreachable actions
            Map<Action, List<Action>> adj = new HashMap<>();
            for (Action a : unreachableActionsList) {
                adj.put(a, new ArrayList<>());
            }
            for (Action a : unreachableActionsList) {
                for (Action b : unreachableActionsList) {
                    if (a != b) {
                        boolean overlaps = false;
                        for (String eff : a.getEffects()) {
                            if (b.getPreconditions().contains(eff)) {
                                overlaps = true;
                                break;
                            }
                        }
                        if (overlaps) {
                            adj.get(a).add(b);
                        }
                    }
                }
            }

            Set<Action> visited = new HashSet<>();
            Set<Action> stack = new HashSet<>();
            List<Action> cycle = new ArrayList<>();
            for (Action a : unreachableActionsList) {
                if (!visited.contains(a)) {
                    if (findCycleDFS(a, adj, visited, stack, cycle)) {
                        StringBuilder cycleStr = new StringBuilder();
                        for (int i = 0; i < cycle.size(); i++) {
                            cycleStr.append(cycle.get(i).getName());
                            if (i < cycle.size() - 1) {
                                cycleStr.append(" -> ");
                            }
                        }
                        return new ValidationResult(false, "Failed to generate plan: Cyclic dependency detected among tasks: " + cycleStr.toString());
                    }
                }
            }

            // Detect disconnected component (preconditions never produced by any action)
            for (Action a : unreachableActionsList) {
                for (String pre : a.getPreconditions()) {
                    if (!reachableFacts.contains(pre)) {
                        boolean produced = false;
                        for (Action other : actions) {
                            if (other.getEffects().contains(pre)) {
                                produced = true;
                                break;
                            }
                        }
                        if (!produced) {
                            return new ValidationResult(false, "Failed to generate plan: Disconnected component. Task '" 
                                    + a.getName() + "' requires precondition '" + pre + "' which is never produced.");
                        }
                    }
                }
            }

            return new ValidationResult(false, "Failed to generate plan: Unreachable goal. Goal conditions " 
                    + missingGoalConditions + " cannot be satisfied.");
        }

        return new ValidationResult(true, null);
    }

    private static boolean findCycleDFS(Action u, Map<Action, List<Action>> adj, Set<Action> visited, Set<Action> stack, List<Action> cycle) {
        visited.add(u);
        stack.add(u);
        cycle.add(u);
        for (Action v : adj.get(u)) {
            if (stack.contains(v)) {
                int idx = cycle.indexOf(v);
                if (idx != -1) {
                    List<Action> sublist = new ArrayList<>(cycle.subList(idx, cycle.size()));
                    sublist.add(v);
                    cycle.clear();
                    cycle.addAll(sublist);
                }
                return true;
            }
            if (!visited.contains(v)) {
                if (findCycleDFS(v, adj, visited, stack, cycle)) {
                    return true;
                }
            }
        }
        stack.remove(u);
        cycle.remove(cycle.size() - 1);
        return false;
    }
}
