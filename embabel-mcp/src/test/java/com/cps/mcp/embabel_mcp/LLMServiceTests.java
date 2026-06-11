package com.cps.mcp.embabel_mcp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.cps.mcp.model.PlanningResponse;
import com.cps.mcp.model.PlanningTask;
import com.cps.mcp.util.LLMService;
import com.cps.mcp.util.LLMServiceFactory;
import com.cps.mcp.util.PlanParser;
import com.cps.mcp.util.GroqClient;
import com.cps.mcp.util.OllamaClient;
import com.cps.mcp.util.AutoClient;

@SpringBootTest
public class LLMServiceTests {

    @Autowired
    private LLMServiceFactory serviceFactory;

    @MockBean
    private GroqClient groqClient;

    @MockBean
    private OllamaClient ollamaClient;

    @Test
    public void testValidJsonParsing() throws Exception {
        String validJson = "{\n" +
                "  \"summary\": \"Plan a party\",\n" +
                "  \"tasks\": [\n" +
                "    {\n" +
                "      \"id\": 1,\n" +
                "      \"title\": \"Search venue\",\n" +
                "      \"description\": \"Find a venue\",\n" +
                "      \"agent\": \"SearchAgent\",\n" +
                "      \"reason\": \"Need location\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        PlanningResponse response = PlanParser.parse(validJson);
        assertNotNull(response);
        assertEquals("Plan a party", response.getSummary());
        assertEquals(1, response.getTasks().size());
        
        PlanningTask task = response.getTasks().get(0);
        assertEquals(1, task.getId());
        assertEquals("Search venue", task.getTitle());
        assertEquals("Find a venue", task.getDescription());
        assertEquals("SearchAgent", task.getAgent());
        assertEquals("Need location", task.getReason());
    }

    @Test
    public void testMalformedJsonParsing() {
        String malformedJson = "{ \"summary\": \"Plan a party\", \"tasks\": [ { \"id\": 1 "; // Unclosed JSON
        assertThrows(Exception.class, () -> {
            PlanParser.parse(malformedJson);
        });
    }

    @Test
    public void testMissingFieldsValidation() {
        String missingSummary = "{\n" +
                "  \"tasks\": [\n" +
                "    {\n" +
                "      \"id\": 1,\n" +
                "      \"title\": \"Search venue\",\n" +
                "      \"description\": \"Find a venue\",\n" +
                "      \"agent\": \"SearchAgent\",\n" +
                "      \"reason\": \"Need location\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        assertThrows(IllegalArgumentException.class, () -> {
            PlanParser.parse(missingSummary);
        });

        String missingTaskTitle = "{\n" +
                "  \"summary\": \"Plan a party\",\n" +
                "  \"tasks\": [\n" +
                "    {\n" +
                "      \"id\": 1,\n" +
                "      \"description\": \"Find a venue\",\n" +
                "      \"agent\": \"SearchAgent\",\n" +
                "      \"reason\": \"Need location\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        assertThrows(IllegalArgumentException.class, () -> {
            PlanParser.parse(missingTaskTitle);
        });
    }

    @Test
    public void testMarkdownFencedJsonRecovery() throws Exception {
        String fencedJson = "```json\n" +
                "{\n" +
                "  \"summary\": \"Plan a party\",\n" +
                "  \"tasks\": [\n" +
                "    {\n" +
                "      \"id\": 1,\n" +
                "      \"title\": \"Search venue\",\n" +
                "      \"description\": \"Find a venue\",\n" +
                "      \"agent\": \"SearchAgent\",\n" +
                "      \"reason\": \"Need location\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n" +
                "```";

        PlanningResponse response = PlanParser.parse(fencedJson);
        assertNotNull(response);
        assertEquals("Plan a party", response.getSummary());
    }

    @Test
    public void testProviderFactoryRouting() {
        LLMService groqService = serviceFactory.getService("groq");
        assertSame(groqClient, groqService);

        LLMService ollamaService = serviceFactory.getService("ollama");
        assertSame(ollamaClient, ollamaService);

        LLMService autoService = serviceFactory.getService("auto");
        assertTrue(autoService instanceof AutoClient);

        LLMService defaultService = serviceFactory.getService(null);
        assertTrue(defaultService instanceof AutoClient);

        LLMService unknownService = serviceFactory.getService("unknown_provider");
        assertTrue(unknownService instanceof AutoClient);
    }

    @Test
    public void testAutoClientGroqSuccess() throws Exception {
        AutoClient autoClient = new AutoClient(groqClient, ollamaClient);
        
        PlanningResponse mockResponse = new PlanningResponse("Groq summary", List.of(new PlanningTask(1, "Title", "Desc", "SearchAgent", "Reason")));
        
        when(groqClient.isConfigured()).thenReturn(true);
        when(groqClient.generatePlan(anyString(), anyString())).thenReturn(mockResponse);

        PlanningResponse result = autoClient.generatePlan("Goal", "Tools");
        assertNotNull(result);
        assertEquals("Groq summary", result.getSummary());
        
        verify(groqClient, times(1)).generatePlan("Goal", "Tools");
        verifyNoInteractions(ollamaClient);
    }

    @Test
    public void testAutoClientGroqFailureOllamaFallback() throws Exception {
        AutoClient autoClient = new AutoClient(groqClient, ollamaClient);
        
        PlanningResponse mockResponse = new PlanningResponse("Ollama summary", List.of(new PlanningTask(1, "Title", "Desc", "SearchAgent", "Reason")));
        
        when(groqClient.isConfigured()).thenReturn(true);
        when(groqClient.generatePlan(anyString(), anyString())).thenThrow(new RuntimeException("Groq error"));
        when(ollamaClient.generatePlan(anyString(), anyString())).thenReturn(mockResponse);

        PlanningResponse result = autoClient.generatePlan("Goal", "Tools");
        assertNotNull(result);
        assertEquals("Ollama summary", result.getSummary());

        verify(groqClient, times(1)).generatePlan("Goal", "Tools");
        verify(ollamaClient, times(1)).generatePlan("Goal", "Tools");
    }

    @Test
    public void testAutoClientCompleteFailurePath() throws Exception {
        AutoClient autoClient = new AutoClient(groqClient, ollamaClient);
        
        when(groqClient.isConfigured()).thenReturn(true);
        when(groqClient.generatePlan(anyString(), anyString())).thenThrow(new RuntimeException("Groq error"));
        when(ollamaClient.generatePlan(anyString(), anyString())).thenThrow(new RuntimeException("Ollama error"));

        assertThrows(Exception.class, () -> {
            autoClient.generatePlan("Goal", "Tools");
        });
    }
}
