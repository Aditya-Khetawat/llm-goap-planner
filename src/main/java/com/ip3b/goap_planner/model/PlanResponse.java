package com.ip3b.goap_planner.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record PlanResponse(
        String goal,
        String summary,
        String status,
        List<PlanStep> steps,
        List<PlanAssignment> assignments,
        String mermaidDiagram,
        String ganttDiagram,
        String source,
        String classification,
        List<Map<String, Object>> trace,
        Instant generatedAt) {
}