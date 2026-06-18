package com.cps.mcp.search.model;

import java.util.Collections;
import java.util.List;

/**
 * A provider-agnostic search response wrapper.
 */
public class SearchResponse {
    private final List<SearchResult> results;
    private final String provider;
    private final String query;
    private final Integer resultCount;
    private final Long timestamp;

    public SearchResponse(List<SearchResult> results) {
        this(results, null, null, null, null);
    }

    public SearchResponse(List<SearchResult> results, String provider, String query, Integer resultCount, Long timestamp) {
        this.results = results != null ? List.copyOf(results) : Collections.emptyList();
        this.provider = provider;
        this.query = query;
        this.resultCount = resultCount;
        this.timestamp = timestamp;
    }

    public List<SearchResult> getResults() {
        return results;
    }

    public String getProvider() {
        return provider;
    }

    public String getQuery() {
        return query;
    }

    public Integer getResultCount() {
        return resultCount;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "SearchResponse{results=" + results +
                ", provider='" + provider + '\'' +
                ", query='" + query + '\'' +
                ", resultCount=" + resultCount +
                ", timestamp=" + timestamp +
                '}';
    }
}
