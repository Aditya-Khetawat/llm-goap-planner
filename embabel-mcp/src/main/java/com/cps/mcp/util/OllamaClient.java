package com.cps.mcp.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cps.mcp.model.PlanningResponse;

@Component
public class OllamaClient implements LLMService {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Value("${ollama.url:http://localhost:11434/api/generate}")
    private String endpoint;

    @Value("${ollama.model:llama3}")
    private String model;

    @Override
    public PlanningResponse generatePlan(String goal, String toolsStr) throws Exception {
        String prompt = "You are a deterministic planning system.\n" +
                        "Your goal: " + goal + "\n" +
                        "Available agents/tools: " + toolsStr + "\n" +
                        "\n" +
                        "You MUST return ONLY a valid JSON object matching the schema below. Do not wrap in markdown or code blocks. Do not explain anything.\n" +
                        "\n" +
                        "Schema:\n" +
                        "{\n" +
                        "  \"summary\": \"Short description of the planning approach\",\n" +
                        "  \"tasks\": [\n" +
                        "    {\n" +
                        "      \"id\": 1,\n" +
                        "      \"title\": \"Short title of the task\",\n" +
                        "      \"description\": \"Detailed description of what to do\",\n" +
                        "      \"agent\": \"Name of the assigned agent (SearchAgent, CalendarAgent, BudgetAgent, InviteAgent, or FoodAgent)\",\n" +
                        "      \"reason\": \"Why this task/agent matches the goal\",\n" +
                        "      \"preconditions\": [\n" +
                        "        \"semantic_fact\"\n" +
                        "      ],\n" +
                        "      \"effects\": [\n" +
                        "        \"semantic_fact\"\n" +
                        "      ]\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}\n" +
                        "\n" +
                        "Preconditions and effects MUST be meaningful, semantic world-state facts (e.g., 'venue_found', 'venue_reserved', 'budget_ready', 'event_finalized') that logically chain together. Do NOT use positional flags like 'step_i_done'. The final step's effects should contain a fact representing the ultimate goal achievement.";

        System.out.println("OllamaClient: Requesting plan generation for goal: " + goal);
        String responseBody = postToOllama(prompt, true);
        
        // Log raw response for debugging
        System.out.println("OllamaClient Raw Response: " + responseBody);

        JsonNode root = mapper.readTree(responseBody);
        String content = root.path("response").asText();
        
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("Empty response returned from local Ollama service");
        }

        return PlanParser.parse(content);
    }

    @Override
    public String simulateAgentExecution(String agentName, String task) throws Exception {
        String prompt = "You are a specialized AI agent named " + agentName + ".\n" +
                        "Your job is to execute the following task and return a brief, realistic response (1-2 sentences) as if you just performed it.\n" +
                        "Do not include code blocks or JSON formatting. Just return a plaintext answer.\n" +
                        "Task: " + task;

        System.out.println("OllamaClient: Simulating agent " + agentName + " for task: " + task);
        String responseBody = postToOllama(prompt, false);
        
        JsonNode root = mapper.readTree(responseBody);
        return root.path("response").asText().trim();
    }

    private String postToOllama(String prompt, boolean requireJson) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", model);
        payload.put("prompt", prompt);
        payload.put("stream", false);

        if (requireJson) {
            payload.put("format", "json");
        }

        String requestBody = mapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(45))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Ollama API returned error status " + response.statusCode() + ": " + response.body());
        }
        return response.body();
    }
}
