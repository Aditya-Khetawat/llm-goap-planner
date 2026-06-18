package com.cps.mcp.util;

import java.util.*;
import com.embabel.plan.common.condition.ConditionAction;
import com.embabel.plan.common.condition.ConditionGoal;
import com.embabel.plan.common.condition.ConditionDetermination;

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

    public static ValidationResult validate(Map<String, Boolean> initialState, ConditionGoal goal, List<ConditionAction> actions) {
        // 1. Self loop (self-dependency) check
        for (ConditionAction action : actions) {
            for (String pre : action.getPreconditions().keySet()) {
                if (action.getEffects().containsKey(pre)) {
                    return new ValidationResult(false, "Invalid plan: Self-dependency detected in task '" 
                            + action.getName() + "' (fact '" + pre + "' is in both preconditions and effects).");
                }
            }
        }

        // 2. Reachability analysis
        Set<String> reachableFacts = new HashSet<>();
        if (initialState != null) {
            for (Map.Entry<String, Boolean> entry : initialState.entrySet()) {
                if (entry.getValue() != null && entry.getValue()) {
                    reachableFacts.add(entry.getKey());
                }
            }
        }

        Set<ConditionAction> reachableActions = new HashSet<>();
        boolean progress = true;
        while (progress) {
            progress = false;
            for (ConditionAction action : actions) {
                if (!reachableActions.contains(action)) {
                    boolean canExecute = true;
                    for (Map.Entry<String, ConditionDetermination> preEntry : action.getPreconditions().entrySet()) {
                        String factName = preEntry.getKey();
                        boolean expected = (preEntry.getValue() == ConditionDetermination.TRUE);
                        boolean actual = reachableFacts.contains(factName);
                        if (actual != expected) {
                            canExecute = false;
                            break;
                        }
                    }
                    if (canExecute) {
                        for (Map.Entry<String, ConditionDetermination> effEntry : action.getEffects().entrySet()) {
                            String factName = effEntry.getKey();
                            if (effEntry.getValue() == ConditionDetermination.TRUE) {
                                reachableFacts.add(factName);
                            } else {
                                reachableFacts.remove(factName);
                            }
                        }
                        reachableActions.add(action);
                        progress = true;
                    }
                }
            }
        }

        // Check if all required goal conditions are satisfied
        List<String> missingGoalConditions = new ArrayList<>();
        for (Map.Entry<String, ConditionDetermination> entry : goal.getPreconditions().entrySet()) {
            String condition = entry.getKey();
            boolean expected = (entry.getValue() == ConditionDetermination.TRUE);
            boolean actual = reachableFacts.contains(condition);
            if (actual != expected) {
                missingGoalConditions.add(condition);
            }
        }

        if (!missingGoalConditions.isEmpty()) {
            // Check if missing goal conditions are produced by any action at all
            for (String missingCond : missingGoalConditions) {
                boolean producedByAny = false;
                for (ConditionAction action : actions) {
                    if (action.getEffects().containsKey(missingCond)) {
                        ConditionDetermination goalDet = goal.getPreconditions().get(missingCond);
                        ConditionDetermination actionDet = action.getEffects().get(missingCond);
                        if (actionDet == goalDet) {
                            producedByAny = true;
                            break;
                        }
                    }
                }
                if (!producedByAny) {
                    return new ValidationResult(false, "Failed to generate plan: Unreachable goal condition. No available action produces '" + missingCond + "'.");
                }
            }

            // Group unreachable actions
            List<ConditionAction> unreachableActionsList = new ArrayList<>();
            for (ConditionAction action : actions) {
                if (!reachableActions.contains(action)) {
                    unreachableActionsList.add(action);
                }
            }

            // Find if there is a cycle among unreachable actions
            Map<ConditionAction, List<ConditionAction>> adj = new HashMap<>();
            for (ConditionAction a : unreachableActionsList) {
                adj.put(a, new ArrayList<>());
            }
            for (ConditionAction a : unreachableActionsList) {
                for (ConditionAction b : unreachableActionsList) {
                    if (a != b) {
                        boolean overlaps = false;
                        for (String eff : a.getEffects().keySet()) {
                            if (b.getPreconditions().containsKey(eff)) {
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

            Set<ConditionAction> visited = new HashSet<>();
            Set<ConditionAction> stack = new HashSet<>();
            List<ConditionAction> cycle = new ArrayList<>();
            for (ConditionAction a : unreachableActionsList) {
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
            for (ConditionAction a : unreachableActionsList) {
                for (String pre : a.getPreconditions().keySet()) {
                    boolean expected = (a.getPreconditions().get(pre) == ConditionDetermination.TRUE);
                    boolean actual = reachableFacts.contains(pre);
                    if (actual != expected) {
                        boolean produced = false;
                        for (ConditionAction other : actions) {
                            if (other.getEffects().containsKey(pre)) {
                                ConditionDetermination otherDet = other.getEffects().get(pre);
                                ConditionDetermination preDet = a.getPreconditions().get(pre);
                                if (otherDet == preDet) {
                                    produced = true;
                                    break;
                                }
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

    private static boolean findCycleDFS(ConditionAction u, Map<ConditionAction, List<ConditionAction>> adj, Set<ConditionAction> visited, Set<ConditionAction> stack, List<ConditionAction> cycle) {
        visited.add(u);
        stack.add(u);
        cycle.add(u);
        for (ConditionAction v : adj.get(u)) {
            if (stack.contains(v)) {
                int idx = cycle.indexOf(v);
                if (idx != -1) {
                    List<ConditionAction> sublist = new ArrayList<>(cycle.subList(idx, cycle.size()));
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

