package com.cps.mcp.search.provider;

import com.cps.mcp.search.model.SearchResponse;
import com.cps.mcp.search.model.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Production-quality Tavily Search API implementation.
 */
public class TavilySearchProvider implements SearchProvider {

    private static final Logger logger = LoggerFactory.getLogger(TavilySearchProvider.class);

    private final String apiKey;
    private final int maxResults;
    private final RestTemplate restTemplate;

    public TavilySearchProvider(String apiKey) {
        this(apiKey, 5);
    }

    public TavilySearchProvider(String apiKey, int maxResults) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Tavily API key is missing or blank");
        }
        this.apiKey = apiKey;
        this.maxResults = maxResults;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5 seconds connect timeout
        factory.setReadTimeout(5000);    // 5 seconds read timeout
        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    public SearchResponse search(String query) throws Exception {
        String url = "https://api.tavily.com/search";
        Map<String, Object> request = Map.of(
                "api_key", apiKey,
                "query", query,
                "search_depth", "basic"
        );

        long startTime = System.currentTimeMillis();
        Map<?, ?> response = null;
        int retriesLeft = 1;

        while (true) {
            try {
                response = restTemplate.postForObject(url, request, Map.class);
                break; // success
            } catch (org.springframework.web.client.HttpClientErrorException e) {
                // HTTP 429 Too Many Requests: retry once
                if (e.getStatusCode().value() == 429 && retriesLeft > 0) {
                    retriesLeft--;
                    logger.warn("Tavily search returned 429 (Too Many Requests). Retrying once...");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }
                // HTTP 401/403 Authentication failures: do not retry
                if (e.getStatusCode().value() == 401 || e.getStatusCode().value() == 403) {
                    throw new RuntimeException("Tavily authentication failed: Invalid API key", e);
                }
                throw new RuntimeException("Tavily search failed (HTTP " + e.getStatusCode().value() + "): " + e.getResponseBodyAsString(), e);
            } catch (org.springframework.web.client.HttpServerErrorException e) {
                throw new RuntimeException("Tavily search failed (HTTP " + e.getStatusCode().value() + "): " + e.getResponseBodyAsString(), e);
            } catch (org.springframework.web.client.ResourceAccessException e) {
                // Transient network / timeout failures: retry once
                if (retriesLeft > 0) {
                    retriesLeft--;
                    logger.warn("Tavily search encountered network failure or timeout. Retrying once... error: {}", e.getMessage());
                    continue;
                }
                throw new RuntimeException("Tavily search request timed out or network unreachable: " + e.getMessage(), e);
            } catch (Exception e) {
                throw new RuntimeException("Tavily search request failed: " + e.getMessage(), e);
            }
        }

        long endTime = System.currentTimeMillis();
        long latency = endTime - startTime;

        if (response == null || !response.containsKey("results")) {
            throw new RuntimeException("Tavily API returned empty or invalid response");
        }

        List<?> resultsList = (List<?>) response.get("results");
        List<SearchResult> searchResults = new ArrayList<>();
        Set<String> seenUrls = new HashSet<>();

        if (resultsList != null) {
            for (Object obj : resultsList) {
                if (obj instanceof Map) {
                    Map<?, ?> map = (Map<?, ?>) obj;
                    String title = (String) map.get("title");
                    String resultUrl = (String) map.get("url");
                    String content = (String) map.get("content");

                    // 1. Trim whitespace
                    title = title != null ? title.trim() : "";
                    resultUrl = resultUrl != null ? resultUrl.trim() : "";
                    content = content != null ? content.trim() : "";

                    // 2. Remove empty titles and URLs
                    if (title.isEmpty() || resultUrl.isEmpty()) {
                        continue;
                    }

                    // 3. Remove duplicate URLs
                    if (!seenUrls.add(resultUrl)) {
                        continue;
                    }

                    searchResults.add(new SearchResult(title, resultUrl, content));

                    // 4. Limit result volume to top N
                    if (searchResults.size() >= maxResults) {
                        break;
                    }
                }
            }
        }

        // Provider-level log
        logger.info("Search Provider: Tavily");
        logger.info("Query: {}", query);
        logger.info("Results: {}", searchResults.size());
        logger.info("Latency: {} ms", latency);

        // Populate metadata
        return new SearchResponse(
                searchResults,
                "tavily",
                query,
                searchResults.size(),
                endTime
        );
    }

    @Override
    public String getName() {
        return "Tavily";
    }
}
