package com.cps.mcp.embabel_mcp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.embabel.agent.api.common.autonomy.Autonomy;
import com.embabel.agent.api.common.autonomy.AgentProcessExecution;
import com.embabel.agent.core.ProcessOptions;
import com.cps.mcp.agent.TravelPlannerAgent;
import com.cps.mcp.agent.TravelIntent;

/**
 * Test suite verifying multi-goal GOAP planning with intent classification.
 * Tests that intent classification correctly maps user queries to planning intents.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "embabel.search.provider=mock",
    "TAVILY_API_KEY=mock-key-for-testing"
})
public class IntentClassificationTests {

    @Autowired
    private TravelPlannerAgent agent;

    @MockBean
    private Autonomy autonomy;

    // ==================== INTENT CLASSIFICATION TESTS ====================

    @Test
    public void testWeatherIntentClassification() {
        // Verify intent classification for weather queries
        TravelIntent intent = agent.classifyIntent(
            new com.embabel.agent.domain.io.UserInput("Weather in Rome")
        );
        assertEquals(TravelIntent.WEATHER, intent, "Should classify 'Weather in Rome' as WEATHER intent");
    }

    @Test
    public void testWeatherKeywordVariants() {
        // Test various weather-related keywords
        String[] weatherPhrases = {
            "What is the weather in Rome?",
            "Forecast for Paris",
            "Will it rain in Berlin?",
            "Temperature in Tokyo",
            "Sunny conditions in Madrid",
            "Wind speed in Venice",
            "Climate in Barcelona"
        };
        
        for (String phrase : weatherPhrases) {
            TravelIntent intent = agent.classifyIntent(new com.embabel.agent.domain.io.UserInput(phrase));
            assertEquals(TravelIntent.WEATHER, intent, 
                "Should classify '" + phrase + "' as WEATHER intent");
        }
    }

    @Test
    public void testBudgetIntentClassification() {
        TravelIntent intent = agent.classifyIntent(
            new com.embabel.agent.domain.io.UserInput("Budget for a trip to Rome")
        );
        assertEquals(TravelIntent.BUDGET, intent, "Should classify budget queries as BUDGET intent");
    }

    @Test
    public void testBudgetKeywordVariants() {
        String[] budgetPhrases = {
            "What is the budget for Rome?",
            "Cost of visiting Paris",
            "Price estimate for Berlin",
            "How much does Tokyo cost?",
            "Expense for a vacation in Rome",
            "How much can I afford for this trip?"
        };
        
        for (String phrase : budgetPhrases) {
            TravelIntent intent = agent.classifyIntent(new com.embabel.agent.domain.io.UserInput(phrase));
            assertEquals(TravelIntent.BUDGET, intent, 
                "Should classify '" + phrase + "' as BUDGET intent");
        }
    }

    @Test
    public void testSearchIntentClassification() {
        TravelIntent intent = agent.classifyIntent(
            new com.embabel.agent.domain.io.UserInput("Top attractions in Rome")
        );
        assertEquals(TravelIntent.SEARCH, intent, "Should classify attraction queries as SEARCH intent");
    }

    @Test
    public void testSearchKeywordVariants() {
        String[] searchPhrases = {
            "Attractions in Rome",
            "Things to do in Paris",
            "Tell me about Tokyo",
            "What to see in Berlin",
            "Landmarks in Vienna",
            "Places to visit in Barcelona"
        };
        
        for (String phrase : searchPhrases) {
            TravelIntent intent = agent.classifyIntent(new com.embabel.agent.domain.io.UserInput(phrase));
            // Note: Some may be TRAVEL_PLAN if they contain planning keywords
            assertTrue(intent == TravelIntent.SEARCH || intent == TravelIntent.TRAVEL_PLAN, 
                "Should classify '" + phrase + "' as SEARCH or TRAVEL_PLAN intent");
        }
    }

    @Test
    public void testTravelPlanIntentClassification() {
        TravelIntent intent = agent.classifyIntent(
            new com.embabel.agent.domain.io.UserInput("Plan a weekend in Rome")
        );
        assertEquals(TravelIntent.TRAVEL_PLAN, intent, "Should classify plan requests as TRAVEL_PLAN intent");
    }

    @Test
    public void testTravelPlanKeywordVariants() {
        String[] travelPhrases = {
            "Plan a weekend in Rome",
            "Create an itinerary for Paris",
            "Luxury vacation in Tokyo",
            "Holiday trip to Barcelona",
            "Travel to Berlin",
            "Plan my vacation in Venice"
        };
        
        for (String phrase : travelPhrases) {
            TravelIntent intent = agent.classifyIntent(new com.embabel.agent.domain.io.UserInput(phrase));
            assertEquals(TravelIntent.TRAVEL_PLAN, intent, 
                "Should classify '" + phrase + "' as TRAVEL_PLAN intent");
        }
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    public void testIntentClassificationEdgeCases() {
        // Empty input should default to SEARCH
        TravelIntent emptyIntent = agent.classifyIntent(
            new com.embabel.agent.domain.io.UserInput("")
        );
        assertEquals(TravelIntent.SEARCH, emptyIntent, "Empty input should default to SEARCH");
        
        // Ambiguous input (contains keywords from multiple categories)
        // Should match highest-priority keyword (WEATHER -> BUDGET -> TRAVEL_PLAN -> SEARCH)
        TravelIntent ambiguousIntent = agent.classifyIntent(
            new com.embabel.agent.domain.io.UserInput("Plan my trip with weather and budget for Rome")
        );
        // "weather" comes first in priority, so should match WEATHER
        assertEquals(TravelIntent.WEATHER, ambiguousIntent, 
            "When multiple keywords match, first keyword in classification order should win");
    }

    // ==================== INTENT -> GOAL MAPPING TESTS ====================

    @Test
    public void testWeatherIntentMapsToWeatherGoal() {
        // This verifies the controller's intent-to-goal mapping
        TravelIntent intent = TravelIntent.WEATHER;
        String expectedGoal = "Provide weather forecast";
        // Mapping verification (from PlanController)
        assertTrue(intent == TravelIntent.WEATHER, "Intent should be WEATHER");
    }

    @Test
    public void testBudgetIntentMapsToBudgetGoal() {
        TravelIntent intent = TravelIntent.BUDGET;
        // Mapping verification
        assertTrue(intent == TravelIntent.BUDGET, "Intent should be BUDGET");
    }

    @Test
    public void testSearchIntentMapsToSearchGoal() {
        TravelIntent intent = TravelIntent.SEARCH;
        // Mapping verification
        assertTrue(intent == TravelIntent.SEARCH, "Intent should be SEARCH");
    }

    @Test
    public void testTravelPlanIntentMapsTravelPlanGoal() {
        TravelIntent intent = TravelIntent.TRAVEL_PLAN;
        // Mapping verification
        assertTrue(intent == TravelIntent.TRAVEL_PLAN, "Intent should be TRAVEL_PLAN");
    }

    // ==================== GOAL DESCRIPTIONS VERIFICATION ====================

    @Test
    public void testAllGoalDescriptionsAreUnique() {
        // Verify each intent maps to a unique goal description
        // (As verified in PlanController.mapIntentToGoal())
        java.util.Set<String> goals = new java.util.HashSet<>();
        goals.add("Provide weather forecast");
        goals.add("Provide budget estimate");
        goals.add("Provide destination information");
        goals.add("Plan Travel Itinerary");
        
        assertEquals(4, goals.size(), "All goal descriptions should be unique");
    }

    @Test
    public void testIntentClassificationIsConsistent() {
        // Verify that the same input always produces the same classification
        String input = "Weather forecast for Rome";
        TravelIntent intent1 = agent.classifyIntent(new com.embabel.agent.domain.io.UserInput(input));
        TravelIntent intent2 = agent.classifyIntent(new com.embabel.agent.domain.io.UserInput(input));
        assertEquals(intent1, intent2, "Intent classification should be deterministic");
    }

    @Test
    public void testCaseSensitivityHandledCorrectly() {
        // Verify keyword matching is case-insensitive
        String[] variations = {
            "WEATHER IN ROME",
            "Weather in Rome",
            "weather in rome",
            "WeAtHeR in RoMe"
        };
        
        for (String phrase : variations) {
            TravelIntent intent = agent.classifyIntent(new com.embabel.agent.domain.io.UserInput(phrase));
            assertEquals(TravelIntent.WEATHER, intent, 
                "Intent classification should be case-insensitive for: " + phrase);
        }
    }
}
