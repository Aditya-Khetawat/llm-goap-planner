package com.cps.mcp.util;

import org.springframework.stereotype.Component;
import com.cps.mcp.model.PlanningResponse;

@Component
public class AutoClient implements LLMService {

    private final GroqClient groqClient;
    private final OllamaClient ollamaClient;

    public AutoClient(GroqClient groqClient, OllamaClient ollamaClient) {
        this.groqClient = groqClient;
        this.ollamaClient = ollamaClient;
    }

    @Override
    public PlanningResponse generatePlan(String goal, String toolsStr) throws Exception {
        if (groqClient.isConfigured()) {
            try {
                System.out.println("AutoClient: Trying plan generation with Groq...");
                return groqClient.generatePlan(goal, toolsStr);
            } catch (Exception e) {
                System.err.println("AutoClient: Groq planning failed, falling back to Ollama. Error: " + e.getMessage());
            }
        } else {
            System.out.println("AutoClient: Groq key not configured, falling back to Ollama.");
        }
        return ollamaClient.generatePlan(goal, toolsStr);
    }

    @Override
    public String simulateAgentExecution(String agentName, String task) throws Exception {
        if (groqClient.isConfigured()) {
            try {
                System.out.println("AutoClient: Trying agent simulation with Groq...");
                return groqClient.simulateAgentExecution(agentName, task);
            } catch (Exception e) {
                System.err.println("AutoClient: Groq simulation failed, falling back to Ollama. Error: " + e.getMessage());
            }
        }
        return ollamaClient.simulateAgentExecution(agentName, task);
    }
}
