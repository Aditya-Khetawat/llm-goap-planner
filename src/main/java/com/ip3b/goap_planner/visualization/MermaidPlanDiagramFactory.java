package com.ip3b.goap_planner.visualization;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Component;

import com.ip3b.goap_planner.model.MermaidGanttTask;
import com.ip3b.goap_planner.model.PlanStep;

@Component
public class MermaidPlanDiagramFactory {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public String buildFlowchart(String goal, List<PlanStep> steps) {
        String safeGoal = escapeMermaid(goal == null || goal.isBlank() ? "Plan" : goal);
        StringBuilder builder = new StringBuilder();
        builder.append("%%{init: {'flowchart': {'nodeSpacing': 50, 'rankSpacing': 40, 'padding': '10'}}}%%\n");
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

    public String buildGantt(String goal, List<MermaidGanttTask> tasks) {
        String safeGoal = escapeMermaid(goal == null || goal.isBlank() ? "Plan" : goal);
        LocalDate startDate = LocalDate.now(ZoneId.systemDefault());

        StringBuilder builder = new StringBuilder();
        builder.append("gantt\n");
        builder.append("    title ").append(safeGoal).append(" timeline\n");
        builder.append("    dateFormat  YYYY-MM-DD\n");
        builder.append("    axisFormat  %b %d\n");
        builder.append("    todayMarker off\n");

        String currentSection = null;
        for (MermaidGanttTask task : tasks) {
            if (!task.section().equals(currentSection)) {
                builder.append("    section ").append(escapeMermaid(task.section())).append("\n");
                currentSection = task.section();
            }

            String taskDate = DATE_FORMATTER.format(startDate.plusDays(task.startOffsetDays()));
            builder.append("    ")
                    .append(escapeMermaid(task.label()))
                    .append(" : ")
                    .append(sanitizeTaskId(task.taskId()))
                    .append(", ")
                    .append(taskDate)
                    .append(", ")
                    .append(task.durationDays())
                    .append("d\n");
        }

        return builder.toString();
    }

    private String sanitizeTaskId(String value) {
        return value == null || value.isBlank()
                ? "task"
                : value.replaceAll("[^A-Za-z0-9_-]", "_");
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
}
