package com.ip3b.goap_planner.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ip3b.goap_planner.model.PlanRequest;
import com.ip3b.goap_planner.model.PlanResponse;
import com.ip3b.goap_planner.model.PlanStep;

@Service
public class PlanService {

    public PlanResponse generatePlan(PlanRequest request) {
        String goal = normalizeGoal(request == null ? null : request.goal());
        String lowerGoal = goal.toLowerCase();

        PlanBlueprint blueprint = selectBlueprint(lowerGoal, goal);

        return new PlanResponse(goal, blueprint.summary(), "Ready", blueprint.steps(), blueprint.mermaidDiagram(), Instant.now());
    }

    private PlanBlueprint selectBlueprint(String lowerGoal, String goal) {
        if (containsAny(lowerGoal, "hackathon", "hack-a-thon", "hack day")) {
            return buildHackathonBlueprint(goal);
        }

        if (containsAny(lowerGoal, "mobile app", "app launch", "launch app", "release app")) {
            return buildMobileAppBlueprint(goal);
        }

        if (containsAny(lowerGoal, "birthday", "party", "celebration")) {
            return buildBirthdayPartyBlueprint(goal);
        }

        return buildGenericBlueprint(goal);
    }

    private String normalizeGoal(String goal) {
        if (goal == null) {
            return "";
        }
        return goal.trim();
    }

    private String buildMermaidDiagram(String goal, List<PlanStep> steps) {
        String safeGoal = escapeMermaid(goal.isBlank() ? "Plan" : goal);
        StringBuilder builder = new StringBuilder();
        builder.append("flowchart TD\n");
        builder.append("    G[Goal: ").append(safeGoal).append("]\n");

        String previousNode = "G";
        for (PlanStep step : steps) {
            String nodeId = "S" + step.order();
            builder.append("    ").append(nodeId).append("[Step ").append(step.order()).append(": ")
                .append(escapeMermaid(step.title())).append("]\n");
            builder.append("    ").append(previousNode).append(" --> ").append(nodeId).append("\n");
            previousNode = nodeId;
        }

        return builder.toString();
    }

    private PlanBlueprint buildHackathonBlueprint(String goal) {
        List<PlanStep> steps = Arrays.asList(
                new PlanStep(1, "Define challenge scope", "Pick a theme, success criteria, and team roles before coding starts."),
                new PlanStep(2, "Split into workstreams", "Assign prototype, pitch, and demo responsibilities so progress can happen in parallel."),
                new PlanStep(3, "Build a demoable core", "Ship the smallest feature set that clearly proves the idea."),
                new PlanStep(4, "Polish the pitch and fallback plan", "Prepare a crisp presentation, backup screenshots, and a stable demo path."));

        String summary = "This hackathon plan prioritizes scope control, parallel execution, and a demo that survives presentation-day surprises.";
        String mermaidDiagram = buildMermaidDiagram(goal, steps);
        return new PlanBlueprint(summary, steps, mermaidDiagram);
    }

    private PlanBlueprint buildMobileAppBlueprint(String goal) {
        List<PlanStep> steps = Arrays.asList(
                new PlanStep(1, "Clarify product promise", "Define the user problem, target audience, and the one thing the app must do well."),
                new PlanStep(2, "Map the first release", "Choose a lean MVP, the onboarding path, and the analytics events to track."),
                new PlanStep(3, "Build and validate flows", "Implement the core screens, connect the backend, and test the happy path on real devices."),
                new PlanStep(4, "Prepare launch assets", "Finish store copy, screenshots, release notes, and the rollout checklist."),
                new PlanStep(5, "Monitor post-launch signals", "Watch crashes, reviews, and retention to decide the first iteration."));

        String summary = "This mobile app plan focuses on product clarity, a minimal launch scope, and the post-release feedback loop.";
        String mermaidDiagram = buildMermaidDiagram(goal, steps).replace("flowchart TD", "flowchart LR");
        return new PlanBlueprint(summary, steps, mermaidDiagram);
    }

    private PlanBlueprint buildBirthdayPartyBlueprint(String goal) {
        List<PlanStep> steps = Arrays.asList(
                new PlanStep(1, "Lock the guest list", "Confirm who is invited, how many people are coming, and any special needs."),
                new PlanStep(2, "Reserve the venue and timing", "Choose the space, set the date, and make sure arrival and setup windows are realistic."),
                new PlanStep(3, "Plan food, cake, and activities", "Coordinate the menu, order the cake, and pick a few easy crowd-pleasers."),
                new PlanStep(4, "Send reminders and prep supplies", "Confirm RSVPs, buy decorations, and pack the items needed on the day."));

        String summary = "This birthday party plan emphasizes logistics, guest coordination, and the small details that make the event feel effortless.";
        String mermaidDiagram = buildPartyMermaidDiagram(goal, steps);
        return new PlanBlueprint(summary, steps, mermaidDiagram);
    }

    private PlanBlueprint buildGenericBlueprint(String goal) {
        List<PlanStep> steps = Arrays.asList(
                new PlanStep(1, "Define the outcome", "Turn the goal into one measurable result that can be checked later."),
                new PlanStep(2, "List constraints and inputs", "Identify the people, tools, time, and dependencies that shape the plan."),
                new PlanStep(3, "Sequence the actions", "Order the work from prerequisites to execution in the smallest viable chain."),
                new PlanStep(4, "Review and adapt", "Compare the result with the goal and decide the next adjustment."));

        String summary = "This starter plan adapts the action sequence to the supplied goal using a simple GOAP-style breakdown.";
        String mermaidDiagram = buildMermaidDiagram(goal, steps);
        return new PlanBlueprint(summary, steps, mermaidDiagram);
    }

    private String buildPartyMermaidDiagram(String goal, List<PlanStep> steps) {
        String safeGoal = escapeMermaid(goal.isBlank() ? "Plan" : goal);
        StringBuilder builder = new StringBuilder();
        builder.append("flowchart TD\n");
        builder.append("    G[Goal: ").append(safeGoal).append("]\n");
        builder.append("    G --> S1[Step 1: ").append(escapeMermaid(steps.get(0).title())).append("]\n");
        builder.append("    G --> S2[Step 2: ").append(escapeMermaid(steps.get(1).title())).append("]\n");
        builder.append("    S1 --> S3[Step 3: ").append(escapeMermaid(steps.get(2).title())).append("]\n");
        builder.append("    S2 --> S4[Step 4: ").append(escapeMermaid(steps.get(3).title())).append("]\n");
        return builder.toString();
    }

    private boolean containsAny(String text, String... candidates) {
        for (String candidate : candidates) {
            if (text.contains(candidate)) {
                return true;
            }
        }

        return false;
    }

    private String escapeMermaid(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("[", "(")
                .replace("]", ")")
                .replace("{", "(")
                .replace("}", ")");
    }

    private record PlanBlueprint(String summary, List<PlanStep> steps, String mermaidDiagram) {
    }
}