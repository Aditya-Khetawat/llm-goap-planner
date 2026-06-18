package com.cps.mcp.embabel_mcp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.embabel.agent.core.ProcessOptions;
import com.embabel.agent.api.common.autonomy.Autonomy;
import com.embabel.agent.api.common.autonomy.AgentProcessExecution;
import com.cps.mcp.agent.TravelPlannerAgent;
import com.cps.mcp.search.model.SearchResponse;
import com.cps.mcp.search.model.SearchResult;
import com.cps.mcp.search.provider.SearchProvider;

import java.util.List;

@SpringBootTest(properties = {
    "embabel.llm.provider=openai",
    "embabel.models.default-llm=gpt-4.1-mini",
    "embabel.search.provider=tavily",
    "embabel.search.tavily.api-key=tvly-dummy-test-key"
})
public class TavilySearchPlannerIntegrationTests {

    private static final Logger logger = LoggerFactory.getLogger(TavilySearchPlannerIntegrationTests.class);

    @org.springframework.boot.test.context.TestConfiguration
    static class TestConfig implements org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor {
        @Override
        public void postProcessBeanDefinitionRegistry(org.springframework.beans.factory.support.BeanDefinitionRegistry registry) {
            if (registry.containsBeanDefinition("llmService")) {
                registry.removeBeanDefinition("llmService");
            }
        }
        @Override
        public void postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory beanFactory) {}
    }

    @Autowired
    private Autonomy autonomy;

    @MockBean
    private SearchProvider searchProvider;

    @Test
    public void testTavilyPlannerIntegrationFlow() throws Exception {
        String destination = "Jaipur";
        String goal = "Plan a 3-day trip to Jaipur";

        when(searchProvider.getName()).thenReturn("Tavily");
        
        SearchResponse mockTavilyResponse = new SearchResponse(
                List.of(
                        new SearchResult("Jaipur Attractions", "https://visitjaipur.com", "Explore the Hawa Mahal and Amber Fort.")
                ),
                "tavily",
                destination,
                1,
                System.currentTimeMillis()
        );
        when(searchProvider.search(eq(destination))).thenReturn(mockTavilyResponse);

        logger.info("Executing autonomy process for: {}", goal);
        AgentProcessExecution execution = autonomy.chooseAndRunAgent(goal, new ProcessOptions());
        assertNotNull(execution, "Execution should not be null");

        TravelPlannerAgent.TravelPlanReport report = (TravelPlannerAgent.TravelPlanReport) execution.getOutput();
        assertNotNull(report, "Plan report should not be null");
        String output = report.content();
        logger.info("Generated Travel Plan Output:\n{}", output);

        assertTrue(output.contains("TRIP PLAN: Jaipur"), "Output should contain destination Jaipur");
        assertTrue(output.contains("Jaipur Attractions"), "Output should contain search result title");
        assertTrue(output.contains("https://visitjaipur.com"), "Output should contain search result URL");
        assertTrue(output.contains("Explore the Hawa Mahal"), "Output should contain search result snippet");
        assertTrue(output.contains("555.00"), "Cost should be calculated correctly");

        verify(searchProvider, times(1)).search(eq(destination));
    }
}
