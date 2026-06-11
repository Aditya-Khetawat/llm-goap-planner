package com.cps.mcp.embabel_mcp;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.Test;

import com.cps.mcp.model.*;

public class GOAPPlannerTests {

    @Test
    public void testSuccessfulGoalResolution() {
        Planner planner = new Planner(new BFSPlanningStrategy());
        State initialState = new State(); // Empty initial state

        // Goal: event_ready
        Goal goal = new Goal(List.of("event_ready"));

        // Actions
        Action action1 = new Action("T1", "FindVenue", "Find venue", List.of(), List.of("venue_found"), null, "SearchAgent");
        Action action2 = new Action("T2", "BookVenue", "Book venue", List.of("venue_found"), List.of("venue_booked"), null, "SearchAgent");
        Action action3 = new Action("T3", "PrepareEvent", "Prepare event", List.of("venue_booked"), List.of("event_ready"), null, "SearchAgent");

        PlanResult result = planner.plan(initialState, goal, List.of(action1, action2, action3));
        List<Action> plan = result.getPlan();

        assertNotNull(plan);
        assertEquals(3, plan.size());
        assertEquals("T1", plan.get(0).getId());
        assertEquals("T2", plan.get(1).getId());
        assertEquals("T3", plan.get(2).getId());

        // Validate trace
        List<Map<String, Object>> trace = result.getTrace();
        assertEquals(3, trace.size());
        assertEquals("FindVenue", trace.get(0).get("action"));
        assertTrue(((List<?>) trace.get(0).get("state_before")).isEmpty());
        assertTrue(((List<?>) trace.get(0).get("missing_preconditions")).isEmpty());
        assertTrue(((List<?>) trace.get(0).get("effects_applied")).contains("venue_found"));
    }

    @Test
    public void testBranchingDependencies() {
        Planner planner = new Planner(new BFSPlanningStrategy());
        State initialState = new State();

        // Goal requires both venue_booked and catering_ordered
        Goal goal = new Goal(List.of("venue_booked", "catering_ordered"));

        // Actions
        Action action1 = new Action("T1", "FindVenue", "Find venue", List.of(), List.of("venue_found"), null, "SearchAgent");
        Action action2 = new Action("T2", "BookVenue", "Book venue", List.of("venue_found"), List.of("venue_booked"), null, "SearchAgent");
        Action action3 = new Action("T3", "OrderCatering", "Order food", List.of("venue_found"), List.of("catering_ordered"), null, "FoodAgent");

        PlanResult result = planner.plan(initialState, goal, List.of(action1, action2, action3));
        List<Action> plan = result.getPlan();

        assertNotNull(plan);
        assertEquals(3, plan.size());
        
        // The first action must be T1 because both T2 and T3 depend on venue_found
        assertEquals("T1", plan.get(0).getId());
        
        // The subsequent plan should contain both T2 and T3 (order doesn't matter as long as both are present)
        Set<String> planIds = new HashSet<>(List.of(plan.get(1).getId(), plan.get(2).getId()));
        assertTrue(planIds.contains("T2"));
        assertTrue(planIds.contains("T3"));
    }

    @Test
    public void testShortestRouteSelection() {
        Planner planner = new Planner(new BFSPlanningStrategy());
        State initialState = new State();

        // Goal: venue_booked
        Goal goal = new Goal(List.of("venue_booked"));

        // Route A: T1 -> T2 (length 2)
        Action action1 = new Action("T1", "FindVenue", "Find venue", List.of(), List.of("venue_found"), null, "SearchAgent");
        Action action2 = new Action("T2", "BookVenue", "Book venue", List.of("venue_found"), List.of("venue_booked"), null, "SearchAgent");

        // Route B: T3 (length 1)
        Action action3 = new Action("T3", "DirectBook", "Direct book", List.of(), List.of("venue_booked"), null, "SearchAgent");

        PlanResult result = planner.plan(initialState, goal, List.of(action1, action2, action3));
        List<Action> plan = result.getPlan();

        assertNotNull(plan);
        // BFS should find the shortest path, which is direct booking (T3)
        assertEquals(1, plan.size());
        assertEquals("T3", plan.get(0).getId());
    }

    @Test
    public void testUnsatisfiedPreconditions() {
        Planner planner = new Planner(new BFSPlanningStrategy());
        State initialState = new State();

        // Goal: venue_booked
        Goal goal = new Goal(List.of("venue_booked"));

        // T2 requires venue_found, but no action produces venue_found
        Action action2 = new Action("T2", "BookVenue", "Book venue", List.of("venue_found"), List.of("venue_booked"), null, "SearchAgent");

        PlanResult result = planner.plan(initialState, goal, List.of(action2));
        List<Action> plan = result.getPlan();

        assertNotNull(plan);
        assertTrue(plan.isEmpty()); // Should fail to find a plan
    }

    @Test
    public void testUnreachableGoal() {
        Planner planner = new Planner(new BFSPlanningStrategy());
        State initialState = new State();

        // Goal requires fact that no action can produce
        Goal goal = new Goal(List.of("impossible_goal_fact"));

        Action action1 = new Action("T1", "FindVenue", "Find venue", List.of(), List.of("venue_found"), null, "SearchAgent");

        PlanResult result = planner.plan(initialState, goal, List.of(action1));
        List<Action> plan = result.getPlan();

        assertNotNull(plan);
        assertTrue(plan.isEmpty());
    }

    @Test
    public void testValidationSuccess() {
        State initialState = new State();
        Goal goal = new Goal(List.of("event_ready"));
        Action action1 = new Action("T1", "FindVenue", "Find venue", List.of(), List.of("venue_found"), null, "SearchAgent");
        Action action2 = new Action("T2", "BookVenue", "Book venue", List.of("venue_found"), List.of("venue_booked"), null, "SearchAgent");
        Action action3 = new Action("T3", "PrepareEvent", "Prepare event", List.of("venue_booked"), List.of("event_ready"), null, "SearchAgent");

        com.cps.mcp.util.PlanValidator.ValidationResult result = com.cps.mcp.util.PlanValidator.validate(initialState, goal, List.of(action1, action2, action3));
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
    }

    @Test
    public void testValidationSelfLoop() {
        State initialState = new State();
        Goal goal = new Goal(List.of("venue_booked"));
        // Action requires venue_booked to book it (self-dependency)
        Action action = new Action("T1", "BookVenue", "Book venue", List.of("venue_booked"), List.of("venue_booked"), null, "SearchAgent");

        com.cps.mcp.util.PlanValidator.ValidationResult result = com.cps.mcp.util.PlanValidator.validate(initialState, goal, List.of(action));
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("Self-dependency detected"));
    }

    @Test
    public void testValidationCyclicDependency() {
        State initialState = new State();
        Goal goal = new Goal(List.of("a"));

        // A needs b, produces a
        Action actionA = new Action("TA", "ActionA", "A", List.of("b"), List.of("a"), null, "SearchAgent");
        // B needs a, produces b
        Action actionB = new Action("TB", "ActionB", "B", List.of("a"), List.of("b"), null, "SearchAgent");

        com.cps.mcp.util.PlanValidator.ValidationResult result = com.cps.mcp.util.PlanValidator.validate(initialState, goal, List.of(actionA, actionB));
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("Cyclic dependency detected"));
    }

    @Test
    public void testValidationDisconnectedComponent() {
        State initialState = new State();
        Goal goal = new Goal(List.of("venue_booked"));

        // BookVenue requires venue_found, but no action produces venue_found
        Action action = new Action("T1", "BookVenue", "Book venue", List.of("venue_found"), List.of("venue_booked"), null, "SearchAgent");

        com.cps.mcp.util.PlanValidator.ValidationResult result = com.cps.mcp.util.PlanValidator.validate(initialState, goal, List.of(action));
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("Disconnected component"));
    }

    @Test
    public void testValidationUnreachableGoal() {
        State initialState = new State();
        // Goal requires fact that no action produces and not in initial state
        Goal goal = new Goal(List.of("unproducible_goal_fact"));

        Action action = new Action("T1", "FindVenue", "Find venue", List.of(), List.of("venue_found"), null, "SearchAgent");

        com.cps.mcp.util.PlanValidator.ValidationResult result = com.cps.mcp.util.PlanValidator.validate(initialState, goal, List.of(action));
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("Unreachable goal condition"));
    }
}
