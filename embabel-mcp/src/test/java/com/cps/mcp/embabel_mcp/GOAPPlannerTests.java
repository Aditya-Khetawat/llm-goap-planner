package com.cps.mcp.embabel_mcp;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.Test;

import com.embabel.plan.common.condition.ConditionAction;
import com.embabel.plan.common.condition.ConditionGoal;
import com.embabel.plan.common.condition.ConditionPlan;
import com.embabel.plan.common.condition.EmbabelPlanningFactory;
import com.embabel.plan.goap.astar.AStarGoapPlanner;

public class GOAPPlannerTests {

    @Test
    public void testSuccessfulGoalResolution() {
        Map<String, Boolean> initialState = new HashMap<>();

        ConditionAction action1 = EmbabelPlanningFactory.createAction("FindVenue", Map.of(), Map.of("venue_found", true));
        ConditionAction action2 = EmbabelPlanningFactory.createAction("BookVenue", Map.of("venue_found", true), Map.of("venue_booked", true));
        ConditionAction action3 = EmbabelPlanningFactory.createAction("PrepareEvent", Map.of("venue_booked", true), Map.of("event_ready", true));

        AStarGoapPlanner planner = new AStarGoapPlanner(EmbabelPlanningFactory.createDeterminer(initialState));
        ConditionGoal goal = EmbabelPlanningFactory.createGoal("Goal", Map.of("event_ready", true));
        ConditionPlan planResult = planner.planToGoal(List.of(action1, action2, action3), goal);
        List<com.embabel.plan.Action> plan = planResult.getActions();

        assertNotNull(plan);
        assertEquals(3, plan.size());
        assertEquals("FindVenue", plan.get(0).getName());
        assertEquals("BookVenue", plan.get(1).getName());
        assertEquals("PrepareEvent", plan.get(2).getName());
    }

    @Test
    public void testBranchingDependencies() {
        Map<String, Boolean> initialState = new HashMap<>();

        ConditionGoal goal = EmbabelPlanningFactory.createGoal("Goal", Map.of("venue_booked", true, "catering_ordered", true));

        ConditionAction action1 = EmbabelPlanningFactory.createAction("FindVenue", Map.of(), Map.of("venue_found", true));
        ConditionAction action2 = EmbabelPlanningFactory.createAction("BookVenue", Map.of("venue_found", true), Map.of("venue_booked", true));
        ConditionAction action3 = EmbabelPlanningFactory.createAction("OrderCatering", Map.of("venue_found", true), Map.of("catering_ordered", true));

        AStarGoapPlanner planner = new AStarGoapPlanner(EmbabelPlanningFactory.createDeterminer(initialState));
        ConditionPlan planResult = planner.planToGoal(List.of(action1, action2, action3), goal);
        List<com.embabel.plan.Action> plan = planResult.getActions();

        assertNotNull(plan);
        assertEquals(3, plan.size());
        assertEquals("FindVenue", plan.get(0).getName());
        
        Set<String> planNames = new HashSet<>(List.of(plan.get(1).getName(), plan.get(2).getName()));
        assertTrue(planNames.contains("BookVenue"));
        assertTrue(planNames.contains("OrderCatering"));
    }

    @Test
    public void testShortestRouteSelection() {
        Map<String, Boolean> initialState = new HashMap<>();

        ConditionGoal goal = EmbabelPlanningFactory.createGoal("Goal", Map.of("venue_booked", true));

        ConditionAction action1 = EmbabelPlanningFactory.createAction("FindVenue", Map.of(), Map.of("venue_found", true));
        ConditionAction action2 = EmbabelPlanningFactory.createAction("BookVenue", Map.of("venue_found", true), Map.of("venue_booked", true));
        ConditionAction action3 = EmbabelPlanningFactory.createAction("DirectBook", Map.of(), Map.of("venue_booked", true));

        AStarGoapPlanner planner = new AStarGoapPlanner(EmbabelPlanningFactory.createDeterminer(initialState));
        ConditionPlan planResult = planner.planToGoal(List.of(action1, action2, action3), goal);
        List<com.embabel.plan.Action> plan = planResult.getActions();

        assertNotNull(plan);
        assertEquals(1, plan.size());
        assertEquals("DirectBook", plan.get(0).getName());
    }

    @Test
    public void testUnsatisfiedPreconditions() {
        Map<String, Boolean> initialState = new HashMap<>();

        ConditionGoal goal = EmbabelPlanningFactory.createGoal("Goal", Map.of("venue_booked", true));
        ConditionAction action2 = EmbabelPlanningFactory.createAction("BookVenue", Map.of("venue_found", true), Map.of("venue_booked", true));

        AStarGoapPlanner planner = new AStarGoapPlanner(EmbabelPlanningFactory.createDeterminer(initialState));
        ConditionPlan planResult = planner.planToGoal(List.of(action2), goal);
        assertTrue(planResult == null || planResult.getActions().isEmpty());
    }

    @Test
    public void testUnreachableGoal() {
        Map<String, Boolean> initialState = new HashMap<>();

        ConditionGoal goal = EmbabelPlanningFactory.createGoal("Goal", Map.of("impossible_goal_fact", true));
        ConditionAction action1 = EmbabelPlanningFactory.createAction("FindVenue", Map.of(), Map.of("venue_found", true));

        AStarGoapPlanner planner = new AStarGoapPlanner(EmbabelPlanningFactory.createDeterminer(initialState));
        ConditionPlan planResult = planner.planToGoal(List.of(action1), goal);
        assertTrue(planResult == null || planResult.getActions().isEmpty());
    }

    @Test
    public void testValidationSuccess() {
        Map<String, Boolean> initialState = new HashMap<>();
        ConditionGoal goal = EmbabelPlanningFactory.createGoal("Goal", Map.of("event_ready", true));
        ConditionAction action1 = EmbabelPlanningFactory.createAction("FindVenue", Map.of(), Map.of("venue_found", true));
        ConditionAction action2 = EmbabelPlanningFactory.createAction("BookVenue", Map.of("venue_found", true), Map.of("venue_booked", true));
        ConditionAction action3 = EmbabelPlanningFactory.createAction("PrepareEvent", Map.of("venue_booked", true), Map.of("event_ready", true));

        com.cps.mcp.util.PlanValidator.ValidationResult result = com.cps.mcp.util.PlanValidator.validate(initialState, goal, List.of(action1, action2, action3));
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
    }

    @Test
    public void testValidationSelfLoop() {
        Map<String, Boolean> initialState = new HashMap<>();
        ConditionGoal goal = EmbabelPlanningFactory.createGoal("Goal", Map.of("venue_booked", true));
        ConditionAction action = EmbabelPlanningFactory.createAction("BookVenue", Map.of("venue_booked", true), Map.of("venue_booked", true));

        com.cps.mcp.util.PlanValidator.ValidationResult result = com.cps.mcp.util.PlanValidator.validate(initialState, goal, List.of(action));
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("Self-dependency detected"));
    }

    @Test
    public void testValidationCyclicDependency() {
        Map<String, Boolean> initialState = new HashMap<>();
        ConditionGoal goal = EmbabelPlanningFactory.createGoal("Goal", Map.of("a", true));

        ConditionAction actionA = EmbabelPlanningFactory.createAction("ActionA", Map.of("b", true), Map.of("a", true));
        ConditionAction actionB = EmbabelPlanningFactory.createAction("ActionB", Map.of("a", true), Map.of("b", true));

        com.cps.mcp.util.PlanValidator.ValidationResult result = com.cps.mcp.util.PlanValidator.validate(initialState, goal, List.of(actionA, actionB));
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("Cyclic dependency detected"));
    }

    @Test
    public void testValidationDisconnectedComponent() {
        Map<String, Boolean> initialState = new HashMap<>();
        ConditionGoal goal = EmbabelPlanningFactory.createGoal("Goal", Map.of("venue_booked", true));

        ConditionAction action = EmbabelPlanningFactory.createAction("BookVenue", Map.of("venue_found", true), Map.of("venue_booked", true));

        com.cps.mcp.util.PlanValidator.ValidationResult result = com.cps.mcp.util.PlanValidator.validate(initialState, goal, List.of(action));
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("Disconnected component"));
    }

    @Test
    public void testValidationUnreachableGoal() {
        Map<String, Boolean> initialState = new HashMap<>();
        ConditionGoal goal = EmbabelPlanningFactory.createGoal("Goal", Map.of("unproducible_goal_fact", true));

        ConditionAction action = EmbabelPlanningFactory.createAction("FindVenue", Map.of(), Map.of("venue_found", true));

        com.cps.mcp.util.PlanValidator.ValidationResult result = com.cps.mcp.util.PlanValidator.validate(initialState, goal, List.of(action));
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("Unreachable goal condition"));
    }
}

