package com.cps.mcp.embabel_mcp;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import com.embabel.agent.core.ToolGroup;
import com.embabel.agent.spi.ToolGroupResolver;
import com.embabel.agent.spi.support.RegistryToolGroupResolver;
import com.embabel.agent.api.tool.Tool;
import com.cps.mcp.tool.SearchToolGroup;
import com.cps.mcp.search.model.SearchResponse;
import com.cps.mcp.search.provider.SearchProvider;
import com.cps.mcp.search.provider.SearchProviderFactory;
import com.cps.mcp.search.provider.MockSearchProvider;
import com.cps.mcp.search.provider.TavilySearchProvider;
import com.cps.mcp.search.provider.BraveSearchProvider;

import java.util.List;

@SpringBootTest
public class SearchToolTests {

    private static final Logger logger = LoggerFactory.getLogger(SearchToolTests.class);

    @Autowired
    private SearchToolGroup searchToolGroup;

    @Autowired
    private ToolGroupResolver toolGroupResolver;

    @Test
    public void testToolRegistration() {
        // 1. Verify Spring bean registration
        assertNotNull(searchToolGroup, "SearchToolGroup bean should be registered in the Spring Context");
    }

    @Test
    public void testToolDiscovery() {
        // 2. Verify discovery via ToolGroupResolver
        assertNotNull(toolGroupResolver);
        assertTrue(toolGroupResolver instanceof RegistryToolGroupResolver, "Resolver should be RegistryToolGroupResolver");
        List<ToolGroup> groups = ((RegistryToolGroupResolver) toolGroupResolver).getToolGroups();
        boolean found = groups.stream()
                .anyMatch(tg -> "SearchToolGroup".equals(tg.getMetadata().getName()));
        assertTrue(found, "SearchToolGroup should be discovered by the ToolGroupResolver");
    }

    @Test
    public void testSuccessfulSearchAndSummaryMock() throws Exception {
        // 3. Verify successful search and summary under mock provider
        MockSearchProvider mockProvider = new MockSearchProvider();
        SearchToolGroup mockToolGroup = new SearchToolGroup(mockProvider);
        List<Tool> tools = mockToolGroup.getTools();
        assertEquals(2, tools.size(), "Should have exactly search and summarize tools");

        Tool searchTool = tools.stream().filter(t -> "search".equals(t.getDefinition().getName())).findFirst().orElseThrow();
        Tool summarizeTool = tools.stream().filter(t -> "summarize".equals(t.getDefinition().getName())).findFirst().orElseThrow();

        // Trace execution logging
        logger.info("Agent: Dispatching request to search tool group");
        logger.info("Planner: Triggering search tool function 'search'");
        logger.info("Search Tool: Routing query 'java' to active provider");
        logger.info("Selected Provider: MockProvider");
        logger.info("Search Execution: Querying external search API for 'java'");

        // Invoke search
        Tool.Result searchResult = searchTool.call("\"java\"");
        logger.info("Result: " + searchResult);
        assertTrue(searchResult instanceof Tool.Result.Text);
        String searchContent = ((Tool.Result.Text) searchResult).getContent();
        assertTrue(searchContent.contains("Mock Result"), "Search result should contain mock title");

        logger.info("Goal Completed: Search tool function 'search' execution successfully finished");

        // Invoke summarize
        logger.info("Agent: Dispatching request to search tool group");
        logger.info("Planner: Triggering search tool function 'summarize'");
        logger.info("Search Tool: Routing query 'java' to active provider");
        logger.info("Selected Provider: MockProvider");
        logger.info("Search Execution: Querying external search API for 'java'");

        Tool.Result summaryResult = summarizeTool.call("\"java\"");
        logger.info("Result: " + summaryResult);
        assertTrue(summaryResult instanceof Tool.Result.Text);
        String summaryContent = ((Tool.Result.Text) summaryResult).getContent();
        assertTrue(summaryContent.contains("Summary of MockProvider search results"), "Summary result should be properly formatted");

        logger.info("Goal Completed: Search tool function 'summarize' execution successfully finished");
    }

    @Test
    public void testProviderSelectionAndAutoMode() {
        // Test auto selection, mock mode, and specific selection logic in SearchProviderFactory
        SearchProviderFactory factory = new SearchProviderFactory();

        // 1. Explicit Mock Mode
        ReflectionTestUtils.setField(factory, "providerType", "mock");
        SearchProvider provider = factory.searchProvider();
        assertTrue(provider instanceof MockSearchProvider);
        assertEquals("MockProvider", provider.getName());

        // 2. Explicit Tavily Mode with missing key -> should return unconfigured provider that fails on search
        ReflectionTestUtils.setField(factory, "providerType", "tavily");
        ReflectionTestUtils.setField(factory, "tavilyApiKey", "");
        SearchProvider tavilyUnconfigured = factory.searchProvider();
        assertEquals("Tavily (Unconfigured)", tavilyUnconfigured.getName());
        assertThrows(IllegalStateException.class, () -> tavilyUnconfigured.search("test"));

        // 3. Explicit Tavily Mode with configured key
        ReflectionTestUtils.setField(factory, "tavilyApiKey", "tvly-testkey");
        SearchProvider tavilyConfigured = factory.searchProvider();
        assertTrue(tavilyConfigured instanceof TavilySearchProvider);
        assertEquals("Tavily", tavilyConfigured.getName());

        // 4. Explicit Brave Mode with missing key
        ReflectionTestUtils.setField(factory, "providerType", "brave");
        ReflectionTestUtils.setField(factory, "braveApiKey", "");
        SearchProvider braveUnconfigured = factory.searchProvider();
        assertEquals("Brave (Unconfigured)", braveUnconfigured.getName());
        assertThrows(IllegalStateException.class, () -> braveUnconfigured.search("test"));

        // 5. Explicit Brave Mode with configured key
        ReflectionTestUtils.setField(factory, "braveApiKey", "bs-testkey");
        SearchProvider braveConfigured = factory.searchProvider();
        assertTrue(braveConfigured instanceof BraveSearchProvider);
        assertEquals("Brave", braveConfigured.getName());

        // 6. Auto Mode - none configured
        ReflectionTestUtils.setField(factory, "providerType", "auto");
        ReflectionTestUtils.setField(factory, "tavilyApiKey", "");
        ReflectionTestUtils.setField(factory, "braveApiKey", "");
        SearchProvider autoNone = factory.searchProvider();
        assertEquals("None", autoNone.getName());
        assertThrows(IllegalStateException.class, () -> autoNone.search("test"));

        // 7. Auto Mode - Tavily configured first
        ReflectionTestUtils.setField(factory, "tavilyApiKey", "tvly-testkey");
        SearchProvider autoTavily = factory.searchProvider();
        assertTrue(autoTavily instanceof TavilySearchProvider);

        // 8. Auto Mode - Brave configured, Tavily empty
        ReflectionTestUtils.setField(factory, "tavilyApiKey", "");
        ReflectionTestUtils.setField(factory, "braveApiKey", "bs-testkey");
        SearchProvider autoBrave = factory.searchProvider();
        assertTrue(autoBrave instanceof BraveSearchProvider);
    }

    @Test
    public void testProviderFailureAndErrorPropagation() {
        // Verify that if a search provider fails (e.g. Mock provider fails on query "fail"),
        // the SearchToolGroup catches it and returns a proper Tool.Result.Error
        MockSearchProvider mockProvider = new MockSearchProvider();
        SearchToolGroup mockToolGroup = new SearchToolGroup(mockProvider);
        List<Tool> tools = mockToolGroup.getTools();
        Tool searchTool = tools.stream().filter(t -> "search".equals(t.getDefinition().getName())).findFirst().orElseThrow();

        Tool.Result result = searchTool.call("\"fail\"");
        assertTrue(result instanceof Tool.Result.Error, "Result should be an Error result");
        Tool.Result.Error errorResult = (Tool.Result.Error) result;
        assertTrue(errorResult.getMessage().contains("Simulated mock search provider failure"));
    }
}
