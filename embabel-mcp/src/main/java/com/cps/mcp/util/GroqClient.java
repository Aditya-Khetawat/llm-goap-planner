package com.cps.mcp.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cps.mcp.model.PlanningResponse;

@Component
public class GroqClient implements LLMService {

    private static final String ENDPOINT = "https://api.groq.com/openai/v1/chat/completions";
    private static final ObjectMapper mapper = new ObjectMapper();

    @Value("${groq.api.key:}")
    private String apiKeyConfig;

    @Value("${groq.model:llama-3.1-8b-instant}")
    private String model;

    public String getApiKey() {
        String key = apiKeyConfig;
        if (key == null || key.trim().isEmpty()) {
            key = System.getenv("GROQ_API_KEY");
        }
        return key != null ? key.trim() : "";
    }

    public boolean isConfigured() {
        String key = getApiKey();
        return key != null && !key.isEmpty();
    }

    @Override
    public PlanningResponse generatePlan(String goal, String toolsStr) throws Exception {
        String apiKey = getApiKey();
        if (apiKey.isEmpty()) {
            throw new IllegalStateException("Groq API Key is not configured. Configure groq.api.key in application.properties or set GROQ_API_KEY environment variable.");
        }

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

        System.out.println("GroqClient: Requesting plan generation for goal: " + goal);
        String responseBody = postToGroq(prompt, true);
        
        // Log raw response for debugging (excluding headers/keys)
        System.out.println("GroqClient Raw Response: " + responseBody);

        JsonNode root = mapper.readTree(responseBody);
        String content = root.path("choices").path(0).path("message").path("content").asText();
        
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("Empty choices or message content returned from Groq API");
        }

        return PlanParser.parse(content);
    }

    @Override
    public String simulateAgentExecution(String agentName, String task) throws Exception {
        String apiKey = getApiKey();
        if (apiKey.isEmpty()) {
            throw new IllegalStateException("Groq API Key is not configured.");
        }

        String prompt = "You are a specialized AI agent named " + agentName + ".\n" +
                        "Your job is to execute the following task and return a brief, realistic response (1-2 sentences) as if you just performed it.\n" +
                        "Do not include code blocks or JSON formatting. Just return a plaintext answer.\n" +
                        "Task: " + task;

        System.out.println("GroqClient: Simulating agent " + agentName + " for task: " + task);
        String responseBody = postToGroq(prompt, false);
        
        JsonNode root = mapper.readTree(responseBody);
        return root.path("choices").path(0).path("message").path("content").asText().trim();
    }

    private String postToGroq(String prompt, boolean requireJson) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", model);
        payload.put("temperature", 0.1);

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> userMsg = new LinkedHashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", prompt);
        messages.add(userMsg);
        payload.put("messages", messages);

        if (requireJson) {
            payload.put("response_format", Map.of("type", "json_object"));
        }

        String requestBody = mapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Groq API returned error status " + response.statusCode() + ": " + response.body());
        }
        return response.body();
    }
}
