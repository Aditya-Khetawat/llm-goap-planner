package com.cps.mcp.agent;

/**
 * Enumeration of travel planning intents recognized by the classifier.
 * Used to determine which goal the Embabel planner should pursue.
 */
public enum TravelIntent {
    /**
     * User wants weather/forecast information for a destination.
     * Examples: "Weather in Rome", "Forecast for Paris", "Temperature in Tokyo"
     */
    WEATHER,

    /**
     * User wants budget/cost estimation for a trip.
     * Examples: "Budget for Rome", "Cost of visiting Paris", "Price estimate for Berlin"
     */
    BUDGET,

    /**
     * User wants general information/attractions for a destination.
     * Examples: "Attractions in Rome", "Things to do in Paris", "Tell me about Tokyo"
     */
    SEARCH,

    /**
     * User wants a complete travel itinerary/plan.
     * Examples: "Plan a weekend in Rome", "Luxury vacation in Tokyo", "Create itinerary for Paris"
     */
    TRAVEL_PLAN
}
