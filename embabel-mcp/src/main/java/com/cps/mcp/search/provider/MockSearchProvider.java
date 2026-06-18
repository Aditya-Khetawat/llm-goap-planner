package com.cps.mcp.search.provider;

import com.cps.mcp.search.model.SearchResponse;
import com.cps.mcp.search.model.SearchResult;

import java.util.List;

/**
 * Mock search provider used for testing or when explicitly configured as the active provider.
 */
public class MockSearchProvider implements SearchProvider {

    @Override
    public SearchResponse search(String query) throws Exception {
        if ("fail".equalsIgnoreCase(query)) {
            throw new RuntimeException("Simulated mock search provider failure");
        }
        SearchResult result1 = new SearchResult(
                "Mock Result: " + query,
                "https://mocksearch.local/result1",
                "This is mock content for the search query '" + query + "'."
        );
        SearchResult result2 = new SearchResult(
                "Another Mock Result: " + query,
                "https://mocksearch.local/result2",
                "This is additional mock data to test structured summarization."
        );
        return new SearchResponse(List.of(result1, result2));
    }

    @Override
    public String getName() {
        return "MockProvider";
    }
}
