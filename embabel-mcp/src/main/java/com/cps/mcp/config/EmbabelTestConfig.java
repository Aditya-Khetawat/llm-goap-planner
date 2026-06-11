package com.cps.mcp.config;

import com.embabel.agent.spi.LlmService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbabelTestConfig {

    @Bean
    public LlmService<?> dummyLlmService() {
        return new DummyLlmService();
    }
}
