package com.cps.mcp.agent;

import com.cps.mcp.search.model.SearchResponse;
import java.util.stream.Collectors;

/**
 * Result DTO for search-only queries.
 * Wraps search results and destination information for user-facing output.
 */
public class SearchReportResult {
    private final SearchResponse searchResponse;
    private final String destination;

    public SearchReportResult(SearchResponse searchResponse, String destination) {
        this.searchResponse = searchResponse;
        this.destination = destination;
    }

    public SearchResponse getSearchResponse() {
        return searchResponse;
    }

    public String getDestination() {
        return destination;
    }

    public String formatContent() {
        if (searchResponse == null || searchResponse.getResults().isEmpty()) {
            return "No information found for " + destination + ".";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("==================================================\n");
        sb.append("DESTINATION INFORMATION: ").append(destination).append("\n");
        sb.append("==================================================\n\n");
        
        sb.append("Search Results:\n");
        sb.append("--------------------------------------------------\n");
        
        String formatted = searchResponse.getResults().stream()
                .map(r -> String.format("- %s\n  URL: %s\n  %s\n", r.getTitle(), r.getUrl(), r.getContent()))
                .collect(Collectors.joining("\n"));
        
        sb.append(formatted);
        sb.append("\nResults Count: ").append(searchResponse.getResults().size()).append("\n");
        
        return sb.toString();
    }

    @Override
    public String toString() {
        return formatContent();
    }
}
