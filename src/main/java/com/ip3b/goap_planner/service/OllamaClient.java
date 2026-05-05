package com.ip3b.goap_planner.service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

/**
 * Client for communicating with Ollama LLM service.
 * Uses Llama3 8B model for plan generation.
 */
@Component
public class OllamaClient {

    private static final String OLLAMA_ENDPOINT = "http://localhost:11434/api/generate";
    private static final String MODEL = "llama3:8b";
    private static final int TIMEOUT_MS = 120000; // 2 minutes

    /**
     * Call Ollama Llama3 model with a prompt
     * @param prompt The input prompt
     * @return The model's response
     */
    public String generate(String prompt) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            URL url = new URL(OLLAMA_ENDPOINT);
            System.out.println("OllamaClient: Target URL = " + OLLAMA_ENDPOINT);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setDoOutput(true);

            // Build request JSON
            Map<String, Object> requestBody = Map.of(
                    "model", MODEL,
                    "prompt", prompt,
                    "stream", false
            );
            String requestJson = mapper.writeValueAsString(requestBody);
            System.out.println("OllamaClient: Request body = " + requestJson);

            // Send request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestJson.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
                os.flush();
            }

            // Read response
            int responseCode = connection.getResponseCode();
            System.out.println("OllamaClient: HTTP response code = " + responseCode);
            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.out.println("OllamaClient: Non-200 response, returning null");
                return null;
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            System.out.println("OllamaClient: Raw response length = " + response.length());
            if (response.length() > 0) {
                System.out.println("OllamaClient: Response preview = " + response.toString().substring(0, Math.min(200, response.length())));
            }

            // Parse response JSON robustly
            Map<String, Object> responseObj = mapper.readValue(response.toString(), Map.class);

            // Common Ollama response shapes vary; attempt several keys
            if (responseObj.containsKey("response") && responseObj.get("response") instanceof String) {
                String result = (String) responseObj.get("response");
                System.out.println("OllamaClient: Found 'response' key, returning string of length " + result.length());
                return result;
            }

            if (responseObj.containsKey("results")) {
                Object results = responseObj.get("results");
                if (results instanceof java.util.List && !((java.util.List) results).isEmpty()) {
                    Object first = ((java.util.List) results).get(0);
                    if (first instanceof Map) {
                        Map firstMap = (Map) first;
                        if (firstMap.containsKey("content")) {
                            return String.valueOf(firstMap.get("content"));
                        }
                        if (firstMap.containsKey("text")) {
                            return String.valueOf(firstMap.get("text"));
                        }
                    } else {
                        return String.valueOf(first);
                    }
                }
            }

            if (responseObj.containsKey("choices")) {
                Object choices = responseObj.get("choices");
                if (choices instanceof java.util.List && !((java.util.List) choices).isEmpty()) {
                    Object first = ((java.util.List) choices).get(0);
                    if (first instanceof Map) {
                        Map firstMap = (Map) first;
                        if (firstMap.containsKey("text")) {
                            return String.valueOf(firstMap.get("text"));
                        }
                        if (firstMap.containsKey("message")) {
                            return String.valueOf(firstMap.get("message"));
                        }
                    } else {
                        return String.valueOf(first);
                    }
                }
            }

            // As a last resort return the full JSON string
            System.out.println("OllamaClient: No recognized response key found, returning full JSON");
            return response.toString();

        } catch (Exception e) {
            System.err.println("OllamaClient: Exception: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Check if Ollama service is available
     * @return true if accessible, false otherwise
     */
    public boolean isAvailable() {
        try {
            URL url = new URL("http://localhost:11434/api/tags");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            int code = connection.getResponseCode();
            return code == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            return false;
        }
    }
}
