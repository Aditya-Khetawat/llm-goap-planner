package com.ip3b.goap_planner.model;

public record MermaidGanttTask(
        String section,
        String label,
        String taskId,
        int startOffsetDays,
        int durationDays) {
}
