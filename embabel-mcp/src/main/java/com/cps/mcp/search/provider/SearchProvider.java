package com.cps.mcp.search.provider;

import com.cps.mcp.search.model.SearchResponse;

/**
 * Interface defining the search provider abstraction.
 * Allows decoupling different search APIs (Mock, Tavily, Brave, etc.) from the tool interface.
 */
public interface SearchProvider {
    /**
     * Executes a search query and returns structured, provider-agnostic search results.
     *
     * @param query the query string
     * @return the structured search response
     * @throws Exception if an API or network error occurs
     */
    SearchResponse search(String query) throws Exception;

    /**
     * Gets the name of the search provider.
     *
     * @return the provider name
     */
    String getName();
}
