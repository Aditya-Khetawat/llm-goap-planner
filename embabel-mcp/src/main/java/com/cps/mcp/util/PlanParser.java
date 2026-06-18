package com.cps.mcp.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cps.mcp.model.PlanningResponse;
import com.cps.mcp.model.PlanningTask;

public class PlanParser {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static PlanningResponse parse(String response) throws Exception {
        if (response == null || response.isBlank()) {
            throw new IllegalArgumentException("Response text is null or empty");
        }
        
        String cleanJson = stripMarkdownFences(response);
        PlanningResponse planResponse = mapper.readValue(cleanJson, PlanningResponse.class);
        
        validate(planResponse);
        return planResponse;
    }

    public static String stripMarkdownFences(String text) {
        if (text == null) {
            return "";
        }
        String trimmed = text.trim();
        // Remove leading ```json or ```
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7).trim();
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3).trim();
        }
        // Remove trailing ```
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3).trim();
        }
        return trimmed.trim();
    }

    public static void validate(PlanningResponse response) {
        if (response == null) {
            throw new IllegalArgumentException("Deserialized planning response is null");
        }
        if (response.getSummary() == null || response.getSummary().trim().isEmpty()) {
            throw new IllegalArgumentException("Validation error: 'summary' is missing or empty");
        }
        if (response.getTasks() == null || response.getTasks().isEmpty()) {
            throw new IllegalArgumentException("Validation error: 'tasks' list is missing or empty");
        }
        
        for (PlanningTask task : response.getTasks()) {
            if (task.getId() <= 0) {
                throw new IllegalArgumentException("Validation error: Task 'id' must be greater than 0");
            }
            if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("Validation error: Task 'title' is missing or empty for task id " + task.getId());
            }
            if (task.getDescription() == null || task.getDescription().trim().isEmpty()) {
                throw new IllegalArgumentException("Validation error: Task 'description' is missing or empty for task id " + task.getId());
            }
            if (task.getAgent() == null || task.getAgent().trim().isEmpty()) {
                throw new IllegalArgumentException("Validation error: Task 'agent' is missing or empty for task id " + task.getId());
            }
            if (task.getReason() == null || task.getReason().trim().isEmpty()) {
                throw new IllegalArgumentException("Validation error: Task 'reason' is missing or empty for task id " + task.getId());
            }
        }
    }
}
