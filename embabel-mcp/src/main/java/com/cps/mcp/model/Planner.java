package com.cps.mcp.model;

import java.util.List;

public class Planner {
    private PlanningStrategy strategy;

    public Planner() {
        this.strategy = new BFSPlanningStrategy(); // default strategy
    }

    public Planner(PlanningStrategy strategy) {
        this.strategy = strategy;
    }

    public PlanningStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(PlanningStrategy strategy) {
        this.strategy = strategy;
    }

    public PlanResult plan(State initialState, Goal goal, List<Action> actions) {
        if (strategy == null) {
            throw new IllegalStateException("PlanningStrategy is not configured");
        }
        return strategy.plan(initialState, goal, actions);
    }
}