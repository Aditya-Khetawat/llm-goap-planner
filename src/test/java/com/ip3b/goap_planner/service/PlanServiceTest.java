package com.ip3b.goap_planner.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.ip3b.goap_planner.model.PlanRequest;
import com.ip3b.goap_planner.model.PlanResponse;
import com.ip3b.goap_planner.visualization.MermaidPlanDiagramFactory;

class PlanServiceTest {

    private final PlanService planService = new PlanService(new MermaidPlanDiagramFactory());

    @Test
    void generatesDifferentPlansForDifferentGoals() {
        PlanResponse hackathon = planService.generatePlan(new PlanRequest("Organize a hackathon"));
        PlanResponse appLaunch = planService.generatePlan(new PlanRequest("Launch a mobile app"));
        PlanResponse birthdayParty = planService.generatePlan(new PlanRequest("Plan a birthday party"));

        assertEquals(4, hackathon.steps().size());
        assertEquals(5, appLaunch.steps().size());
        assertEquals(4, birthdayParty.steps().size());

        assertNotEquals(hackathon.summary(), appLaunch.summary());
        assertNotEquals(appLaunch.summary(), birthdayParty.summary());
        assertNotEquals(hackathon.mermaidDiagram(), appLaunch.mermaidDiagram());
        assertNotEquals(appLaunch.mermaidDiagram(), birthdayParty.mermaidDiagram());
        assertNotEquals(hackathon.ganttDiagram(), appLaunch.ganttDiagram());
        assertNotEquals(appLaunch.ganttDiagram(), birthdayParty.ganttDiagram());

        assertTrue(hackathon.ganttDiagram().contains("gantt"));
        assertTrue(appLaunch.ganttDiagram().contains("gantt"));
        assertTrue(birthdayParty.ganttDiagram().contains("gantt"));

        assertTrue(hackathon.steps().get(0).title().toLowerCase().contains("challenge"));
        assertTrue(appLaunch.steps().get(0).title().toLowerCase().contains("product"));
        assertTrue(birthdayParty.steps().get(0).title().toLowerCase().contains("guest"));
    }
}