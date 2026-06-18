package com.cps.mcp.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.embabel.agent.spi.LlmService;
import com.embabel.agent.spi.support.springai.SpringAiLlmService;

@Configuration
public class EmbabelLlmConfig {

    @Value("${embabel.llm.provider:groq}")
    private String provider;

    @Value("${embabel.models.default-llm:llama-3.1-8b-instant}")
    private String defaultLlm;

    @Bean
    public LlmService<?> llmService(ChatModel chatModel) {
        return new SpringAiLlmService(defaultLlm, provider, chatModel);
    }
}
