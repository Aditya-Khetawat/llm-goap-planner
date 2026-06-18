package com.cps.mcp.util;

import org.springframework.stereotype.Component;

@Component
public class LLMServiceFactory {

    private final GroqClient groqClient;
    private final OllamaClient ollamaClient;
    private final AutoClient autoClient;

    public LLMServiceFactory(GroqClient groqClient, OllamaClient ollamaClient, AutoClient autoClient) {
        this.groqClient = groqClient;
        this.ollamaClient = ollamaClient;
        this.autoClient = autoClient;
    }

    public LLMService getService(String provider) {
        if (provider == null) {
            return autoClient;
        }

        String normalized = provider.trim().toLowerCase();
        switch (normalized) {
            case "groq":
                return groqClient;
            case "ollama":
                return ollamaClient;
            case "auto":
                return autoClient;
            default:
                System.out.println("LLMServiceFactory: Unknown provider '" + provider + "'. Defaulting to 'auto'.");
                return autoClient;
        }
    }
}
