package com.cps.mcp.model;

import java.util.List;

public interface PlanningStrategy {
    PlanResult plan(State initialState, Goal goal, List<Action> actions);
}
