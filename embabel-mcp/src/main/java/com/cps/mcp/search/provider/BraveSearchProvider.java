package com.cps.mcp.search.provider;

import com.cps.mcp.search.model.SearchResponse;
import com.cps.mcp.search.model.SearchResult;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Brave Search API implementation.
 */
public class BraveSearchProvider implements SearchProvider {

    private final String apiKey;
    private final RestTemplate restTemplate;

    public BraveSearchProvider(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Brave Search API key is missing or blank");
        }
        this.apiKey = apiKey;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public SearchResponse search(String query) throws Exception {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://api.search.brave.com/res/v1/web/search?q=" + encodedQuery;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("X-Subscription-Token", apiKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<?, ?> response = responseEntity.getBody();
            if (response == null || !response.containsKey("web")) {
                throw new RuntimeException("Brave Search API returned empty or invalid response");
            }
            Map<?, ?> web = (Map<?, ?>) response.get("web");
            if (web == null || !web.containsKey("results")) {
                return new SearchResponse(List.of());
            }
            List<?> resultsList = (List<?>) web.get("results");
            List<SearchResult> searchResults = new ArrayList<>();
            if (resultsList != null) {
                for (Object obj : resultsList) {
                    if (obj instanceof Map) {
                        Map<?, ?> map = (Map<?, ?>) obj;
                        String title = (String) map.get("title");
                        String resultUrl = (String) map.get("url");
                        String description = (String) map.get("description");
                        searchResults.add(new SearchResult(title, resultUrl, description));
                    }
                }
            }
            return new SearchResponse(searchResults);
        } catch (Exception e) {
            throw new RuntimeException("Brave search request failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String getName() {
        return "Brave";
    }
}
