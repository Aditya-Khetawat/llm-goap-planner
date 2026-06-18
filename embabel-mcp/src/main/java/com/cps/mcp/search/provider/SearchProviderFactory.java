package com.cps.mcp.search.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cps.mcp.search.model.SearchResponse;

/**
 * Spring configuration factory for selecting and configuring the active SearchProvider.
 * Conforms to configuration rules for mock, tavily, brave, and auto-selection modes.
 */
@Configuration
public class SearchProviderFactory {

    @Value("${embabel.search.provider:auto}")
    private String providerType;

    @Value("${embabel.search.tavily.api-key:}")
    private String tavilyApiKey;

    @Value("${embabel.search.brave.api-key:}")
    private String braveApiKey;

    @Value("${embabel.search.max-results:5}")
    private int maxResults;

    @Bean
    public SearchProvider searchProvider() {
        String type = providerType != null ? providerType.trim().toLowerCase() : "auto";

        if ("mock".equals(type)) {
            return new MockSearchProvider();
        }

        if ("tavily".equals(type)) {
            if (tavilyApiKey == null || tavilyApiKey.trim().isEmpty() || "${TAVILY_API_KEY}".equals(tavilyApiKey)) {
                return new SearchProvider() {
                    @Override
                    public SearchResponse search(String query) throws Exception {
                        throw new IllegalStateException("Tavily provider configured but API key (embabel.search.tavily.api-key) is missing.");
                    }
                    @Override
                    public String getName() {
                        return "Tavily (Unconfigured)";
                    }
                };
            }
            return new TavilySearchProvider(tavilyApiKey, maxResults);
        }

        if ("brave".equals(type)) {
            if (braveApiKey == null || braveApiKey.trim().isEmpty() || "${BRAVE_API_KEY}".equals(braveApiKey)) {
                return new SearchProvider() {
                    @Override
                    public SearchResponse search(String query) throws Exception {
                        throw new IllegalStateException("Brave provider configured but API key (embabel.search.brave.api-key) is missing.");
                    }
                    @Override
                    public String getName() {
                        return "Brave (Unconfigured)";
                    }
                };
            }
            return new BraveSearchProvider(braveApiKey);
        }

        // Auto provider selection: choose the first properly configured provider
        boolean hasTavily = tavilyApiKey != null && !tavilyApiKey.trim().isEmpty() && !"${TAVILY_API_KEY}".equals(tavilyApiKey);
        boolean hasBrave = braveApiKey != null && !braveApiKey.trim().isEmpty() && !"${BRAVE_API_KEY}".equals(braveApiKey);

        if (hasTavily) {
            return new TavilySearchProvider(tavilyApiKey, maxResults);
        }
        if (hasBrave) {
            return new BraveSearchProvider(braveApiKey);
        }

        // If none are configured, throw a structured error on invocation
        return new SearchProvider() {
            @Override
            public SearchResponse search(String query) throws Exception {
                throw new IllegalStateException("No search provider is configured. Please configure Tavily or Brave API keys, or set embabel.search.provider=mock.");
            }
            @Override
            public String getName() {
                return "None";
            }
        };
    }
}
