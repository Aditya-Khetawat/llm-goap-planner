package com.cps.mcp.tool;

import com.embabel.agent.core.ToolGroup;
import com.embabel.agent.core.ToolGroupMetadata;
import com.embabel.agent.api.tool.Tool;
import com.cps.mcp.search.model.SearchResponse;
import com.cps.mcp.search.model.SearchResult;
import com.cps.mcp.search.provider.SearchProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Production Search Tool Group implementing Embabel's ToolGroup interface.
 * Exposes web search and summarization tools.
 */
@Component
public class SearchToolGroup implements ToolGroup {

    private static final Logger logger = LoggerFactory.getLogger(SearchToolGroup.class);
    private final ToolGroupMetadata metadata;
    private final SearchProvider searchProvider;

    public SearchToolGroup(SearchProvider searchProvider) {
        this.searchProvider = searchProvider;

        // We use java.lang.reflect.Proxy to implement ToolGroupMetadata dynamically 
        // to bypass Kotlin inline-class compiled name-mangling for getVersion-Id9oKnY()
        this.metadata = (ToolGroupMetadata) Proxy.newProxyInstance(
                ToolGroupMetadata.class.getClassLoader(),
                new Class<?>[]{ToolGroupMetadata.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getRole":
                            return "search";
                        case "getDescription":
                            return "Search the web and summarize search results";
                        case "getName":
                            return "SearchToolGroup";
                        case "getProvider":
                            return "local";
                        case "getPermissions":
                            return Collections.emptySet();
                        case "infoString":
                            return "SearchToolGroup 1.0.0";
                        case "toString":
                            return "SearchToolGroup";
                        default:
                            if (method.getName().contains("getVersion")) {
                                return "1.0.0";
                            }
                            return null;
                    }
                }
        );
    }

    @Override
    public ToolGroupMetadata getMetadata() {
        return metadata;
    }

    @Override
    public List<Tool> getTools() {
        Tool searchTool = Tool.fromFunction(
                "search",
                "Executes a web search query and returns structured results",
                String.class,
                String.class,
                query -> {
                    logTraceStart("search", query);
                    try {
                        SearchResponse response = searchProvider.search(query);
                        logTraceResult("search", response);
                        String output = formatResponse(response);
                        logTraceCompleted("search");
                        return output;
                    } catch (Exception e) {
                        logger.error("Search Tool: Error executing search query '{}': {}", query, e.getMessage());
                        throw new RuntimeException("Search failed: " + e.getMessage(), e);
                    }
                }
        );

        Tool summarizeTool = Tool.fromFunction(
                "summarize",
                "Summarizes web search results for a given query",
                String.class,
                String.class,
                query -> {
                    logTraceStart("summarize", query);
                    try {
                        SearchResponse response = searchProvider.search(query);
                        logTraceResult("summarize", response);
                        String summary = formatSummary(query, response);
                        logTraceCompleted("summarize");
                        return summary;
                    } catch (Exception e) {
                        logger.error("Search Tool: Error generating summary for query '{}': {}", query, e.getMessage());
                        throw new RuntimeException("Summarization failed: " + e.getMessage(), e);
                    }
                }
        );

        return List.of(searchTool, summarizeTool);
    }

    private void logTraceStart(String toolName, String query) {
        logger.info("Agent: Dispatching request to search tool group");
        logger.info("Planner: Triggering search tool function '{}'", toolName);
        logger.info("Search Tool: Routing query '{}' to active provider", query);
        logger.info("Selected Provider: {}", searchProvider.getName());
        logger.info("Search Execution: Querying external search API for '{}'", query);
    }

    private void logTraceResult(String toolName, SearchResponse response) {
        logger.info("Result: Retrieved {} structured result(s) from provider", response.getResults().size());
    }

    private void logTraceCompleted(String toolName) {
        logger.info("Goal Completed: Search tool function '{}' execution successfully finished", toolName);
    }

    private String formatResponse(SearchResponse response) {
        if (response.getResults().isEmpty()) {
            return "No search results found.";
        }
        return response.getResults().stream()
                .map(r -> String.format("- Title: %s\n  URL: %s\n  Snippet: %s", r.getTitle(), r.getUrl(), r.getContent()))
                .collect(Collectors.joining("\n\n"));
    }

    private String formatSummary(String query, SearchResponse response) {
        if (response.getResults().isEmpty()) {
            return "No search results found to summarize for query '" + query + "'.";
        }
        String combined = response.getResults().stream()
                .map(SearchResult::getContent)
                .collect(Collectors.joining(" "));
        return String.format("Summary of %s search results for '%s': %s",
                searchProvider.getName(), query, combined);
    }
}
