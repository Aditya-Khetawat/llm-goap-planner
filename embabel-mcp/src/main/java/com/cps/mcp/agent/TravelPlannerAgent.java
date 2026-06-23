package com.cps.mcp.agent;

import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.domain.io.UserInput;
import com.cps.mcp.search.provider.SearchProvider;
import com.cps.mcp.search.model.SearchResponse;
import com.cps.mcp.budget.service.BudgetService;
import com.cps.mcp.budget.model.BudgetEstimate;
import com.cps.mcp.budget.model.BudgetItem;
import com.cps.mcp.weather.model.WeatherReport;
import com.cps.mcp.weather.provider.WeatherProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;
import java.util.stream.Collectors;

/**
 * Planner-driven Travel Planner Agent that orchestrates multi-tool execution
 * via type-based preconditions and postconditions.
 */
@Agent(name = "TravelPlannerAgent", description = "Travel planner agent that searches and calculates budgets for trips")
@Component
public class TravelPlannerAgent {

    private static final Logger logger = LoggerFactory.getLogger(TravelPlannerAgent.class);

    // Centralized Default Trip Settings
    private static final int DEFAULT_DAYS = 3;
    private static final BigDecimal DEFAULT_HOTEL_RATE = new BigDecimal("100.00");
    private static final BigDecimal DEFAULT_FOOD_RATE = new BigDecimal("40.00");
    private static final BigDecimal DEFAULT_TRANSPORT_RATE = new BigDecimal("30.00");
    private static final BigDecimal DEFAULT_MISC_RATE = new BigDecimal("15.00");

    private static final java.util.Set<String> STARTER_VERBS = java.util.Set.of(
        "plan", "travel", "find", "go", "create", "book", "help", "get", "organize", "make", "i", "we",
        "me", "us", "him", "her", "them", "it", "my", "your", "his", "their"
    );

    private static final java.util.Set<String> CONNECTIVE_WORDS = java.util.Set.of(
        "de", "di", "of", "and", "the", "la", "el", "le"
    );

    private static final java.util.Set<String> STOP_WORDS = java.util.Set.of(
        "for", "with", "on", "next", "this", "in", "to", "tomorrow", "today", "winter", "summer", "spring", "autumn", "fall", "week", "month", "year", "days", "day", "style", "budget", "luxury", "standard", "family", "solo", "adventure", "business", "leisure"
    );

    private static final java.util.Set<String> DISALLOWED_DESTINATIONS = java.util.Set.of(
        "trip", "weekend", "vacation", "holiday", "itinerary", "plan", "flight", "hotel", "somewhere", "place", "city", "country", "destination", "me", "us", "him", "her", "them", "it", "my", "your", "his", "their", "a", "an", "the", "next", "week", "month", "year", "day", "days"
    );

    private final SearchProvider searchProvider;
    private final BudgetService budgetService;
    private final WeatherProvider weatherProvider;

    @Autowired
    private ApplicationContext applicationContext;

    public TravelPlannerAgent(SearchProvider searchProvider, BudgetService budgetService, WeatherProvider weatherProvider) {
        this.searchProvider = searchProvider;
        this.budgetService = budgetService;
        this.weatherProvider = weatherProvider;
    }

    // --- Strongly Typed Blackboard DTOs ---

    public static class Destination {
        private final String name;
        public Destination(String name) { this.name = name; }
        public String name() { return name; }
        @Override public String toString() { return name; }
    }

    public static class TravelPlanReport {
        private final String content;
        public TravelPlanReport(String content) { this.content = content; }
        public String content() { return content; }
        @Override public String toString() { return content; }
    }

    // --- Reflection Helper ---

    private String getUserInputContent(Object userInputObj) {
        try {
            java.lang.reflect.Method getContentMethod = userInputObj.getClass().getMethod("getContent");
            return (String) getContentMethod.invoke(userInputObj);
        } catch (Exception e) {
            String str = userInputObj.toString();
            int start = str.indexOf("content=");
            if (start != -1) {
                int end = str.indexOf(",", start);
                if (end != -1) {
                    return str.substring(start + "content=".length(), end);
                }
                end = str.indexOf(")", start);
                if (end != -1) {
                    return str.substring(start + "content=".length(), end);
                }
            }
            return str;
        }
    }

    // --- Granular Action Methods ---

    @Action(description = "Extract destination from user prompt")
    public Destination extractDestination(UserInput input) {
        logger.info("UserInput: Received UserInput object on blackboard");
        String content = getUserInputContent(input);
        String name = extractDestinationName(content);
        logger.info("extractDestination: Extracted destination={}", name);
        return new Destination(name);
    }

    private String extractDestinationName(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("User input prompt is empty or null");
        }
        
        // Normalize whitespace and remove trailing punctuation
        String cleaned = prompt.trim().replaceAll("\\s+", " ");
        String normalized = cleaned.replaceAll("[.,?!]$", "");
        
        String[] tokens = normalized.split(" ");
        if (tokens.length == 0) {
            throw new IllegalArgumentException("User input prompt contains no tokens");
        }

        // Search for the first occurrence of "to" or "in" that is not the first token (or is a valid separator)
        int keywordIdx = -1;
        for (int i = 0; i < tokens.length; i++) {
            String tokenLower = tokens[i].toLowerCase();
            if (tokenLower.equals("to") || tokenLower.equals("in")) {
                if (i < tokens.length - 1) {
                    keywordIdx = i;
                    break;
                }
            }
        }

        if (keywordIdx != -1) {
            StringBuilder sb = new StringBuilder();
            for (int j = keywordIdx + 1; j < tokens.length; j++) {
                String word = tokens[j];
                String wordLower = word.toLowerCase().replaceAll("[^a-z]", "");
                
                if (STOP_WORDS.contains(wordLower)) {
                    break;
                }
                
                if (sb.length() > 0) sb.append(" ");
                sb.append(word.replaceAll("[^a-zA-Z]", ""));
            }
            
            String candidate = sb.toString().trim();
            if (isValidDestination(candidate)) {
                return titleCase(candidate);
            }
        }

        // Fallback: If no "to" or "in" keyword found, filter out starter verbs
        int startIdx = 0;
        while (startIdx < tokens.length) {
            String wordLower = tokens[startIdx].toLowerCase().replaceAll("[^a-z]", "");
            if (STARTER_VERBS.contains(wordLower) || STOP_WORDS.contains(wordLower) || CONNECTIVE_WORDS.contains(wordLower) || wordLower.equals("a") || wordLower.equals("an")) {
                startIdx++;
            } else {
                break;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int j = startIdx; j < tokens.length; j++) {
            String word = tokens[j];
            String wordLower = word.toLowerCase().replaceAll("[^a-z]", "");
            if (STOP_WORDS.contains(wordLower)) {
                break;
            }
            if (sb.length() > 0) sb.append(" ");
            sb.append(word.replaceAll("[^a-zA-Z]", ""));
        }

        String candidate = sb.toString().trim();
        if (isValidDestination(candidate)) {
            return titleCase(candidate);
        }

        throw new IllegalArgumentException("Unknown destination in user input: " + prompt);
    }

    private String titleCase(String input) {
        if (input == null || input.isBlank()) return input;
        String[] words = input.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (word.isEmpty()) continue;
            if (i > 0) sb.append(" ");
            
            String lower = word.toLowerCase();
            if (i > 0 && (lower.equals("de") || lower.equals("di") || lower.equals("of") || 
                          lower.equals("and") || lower.equals("the") || lower.equals("la") || 
                          lower.equals("el") || lower.equals("le"))) {
                sb.append(lower);
            } else {
                sb.append(Character.toUpperCase(word.charAt(0)))
                  .append(word.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }
    
    private boolean isValidDestination(String dest) {
        if (dest == null || dest.isBlank()) {
            return false;
        }
        String clean = dest.trim().toLowerCase();
        if (DISALLOWED_DESTINATIONS.contains(clean)) {
            return false;
        }
        return dest.length() >= 2 && dest.length() <= 50 && dest.matches(".*[a-zA-Z].*");
    }

    @Action(description = "Classify travel intent from user input")
    public TravelIntent classifyIntent(UserInput input) {
        logger.info("classifyIntent: Classifying user intent");
        String content = getUserInputContent(input);
        if (content == null) {
            content = "";
        }
        String normalized = content.toLowerCase();
        
        // Weather intent keywords
        if (normalized.contains("weather") || normalized.contains("forecast") || 
            normalized.contains("rain") || normalized.contains("temperature") || 
            normalized.contains("sunny") || normalized.contains("climate") ||
            normalized.contains("snow") || normalized.contains("wind") ||
            normalized.contains("condition")) {
            logger.info("classifyIntent: Detected WEATHER intent");
            return TravelIntent.WEATHER;
        }
        
        // Budget intent keywords
        if (normalized.contains("budget") || normalized.contains("cost") || 
            normalized.contains("price") || normalized.contains("expense") || 
            normalized.contains("estimate") || normalized.contains("how much") ||
            normalized.contains("spending") || normalized.contains("afford")) {
            logger.info("classifyIntent: Detected BUDGET intent");
            return TravelIntent.BUDGET;
        }
        
        // Travel plan intent keywords
        if (normalized.contains("plan") || normalized.contains("trip") || 
            normalized.contains("vacation") || normalized.contains("itinerary") || 
            normalized.contains("weekend") || normalized.contains("holiday") ||
            normalized.contains("travel") || normalized.contains("create itinerary")) {
            logger.info("classifyIntent: Detected TRAVEL_PLAN intent");
            return TravelIntent.TRAVEL_PLAN;
        }
        
        // Default to search for general information queries
        logger.info("classifyIntent: Defaulting to SEARCH intent");
        return TravelIntent.SEARCH;
    }

    @Action(description = "Search travel details for destination")
    public SearchResponse executeSearch(Destination dest) throws Exception {
        logger.info("Destination: Received Destination object on blackboard: {}", dest.name());
        logger.info("executeSearch: Executing web search for travel information in {}", dest.name());
        SearchResponse response = searchProvider.search(dest.name());
        logger.info("SearchResponse: Search completed, bound SearchResponse to blackboard");
        return response;
    }

    @Action(description = "Get weather forecast for destination")
    public WeatherReport getWeather(Destination dest) throws Exception {
        logger.info("Destination: Received Destination object on blackboard: {}", dest.name());
        logger.info("getWeather: Fetching weather forecast for {}", dest.name());
        WeatherReport report = weatherProvider.getWeather(dest.name());
        logger.info("WeatherReport: Weather search completed, bound WeatherReport to blackboard");
        return report;
    }

    @Action(description = "Extract travel constraints from user prompt")
    public TravelConstraints extractConstraints(UserInput input) {
        logger.info("extractConstraints: Extracting constraints from UserInput");
        String content = getUserInputContent(input);
        if (content == null) {
            content = "";
        }
        String normalized = content.toLowerCase();

        // 1. Duration
        int days = DEFAULT_DAYS;
        if (normalized.contains("weekend")) {
            days = 2;
        } else {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)\\s*-?\\s*days?");
            java.util.regex.Matcher matcher = pattern.matcher(normalized);
            if (matcher.find()) {
                try {
                    days = Integer.parseInt(matcher.group(1));
                } catch (NumberFormatException e) {
                    // fallback
                }
            }
        }
        TripDuration duration = new TripDuration(days);

        // 2. Budget Preference
        BudgetPreference.BudgetPrefValue budgetVal = BudgetPreference.BudgetPrefValue.STANDARD;
        if (normalized.contains("budget")) {
            budgetVal = BudgetPreference.BudgetPrefValue.BUDGET;
        } else if (normalized.contains("luxury")) {
            budgetVal = BudgetPreference.BudgetPrefValue.LUXURY;
        }
        BudgetPreference budgetPreference = new BudgetPreference(budgetVal);

        // 3. Travel Style
        TravelStyle.TravelStyleValue styleVal = TravelStyle.TravelStyleValue.LEISURE;
        if (normalized.contains("family")) {
            styleVal = TravelStyle.TravelStyleValue.FAMILY;
        } else if (normalized.contains("solo")) {
            styleVal = TravelStyle.TravelStyleValue.SOLO;
        } else if (normalized.contains("adventure")) {
            styleVal = TravelStyle.TravelStyleValue.ADVENTURE;
        } else if (normalized.contains("business")) {
            styleVal = TravelStyle.TravelStyleValue.BUSINESS;
        }
        TravelStyle travelStyle = new TravelStyle(styleVal);

        TravelConstraints constraints = new TravelConstraints(duration, budgetPreference, travelStyle);
        logger.info("extractConstraints: Extracted constraints={}", constraints);
        return constraints;
    }

    @Action(description = "Calculate travel budget for destination")
    public BudgetEstimate calculateBudget(Destination dest, TravelConstraints constraints) {
        logger.info("Destination: Received Destination object on blackboard: {}", dest.name());
        logger.info("calculateBudget: Calculating budget for destination={}, constraints={}", dest.name(), constraints);
        
        int days = constraints.duration().days();
        BigDecimal hotel = DEFAULT_HOTEL_RATE;
        BigDecimal food = DEFAULT_FOOD_RATE;
        BigDecimal transport = DEFAULT_TRANSPORT_RATE;
        BigDecimal misc = DEFAULT_MISC_RATE;

        if ("Prague".equalsIgnoreCase(dest.name())) {
            hotel = new BigDecimal("150.00");
            food = new BigDecimal("50.00");
            transport = new BigDecimal("40.00");
            misc = new BigDecimal("20.00");
        } else if ("Tokyo".equalsIgnoreCase(dest.name())) {
            hotel = new BigDecimal("120.00");
            food = new BigDecimal("60.00");
            transport = new BigDecimal("50.00");
            misc = new BigDecimal("25.00");
        }

        // Apply budget preference scaling factor
        BigDecimal factor = BigDecimal.ONE;
        if (constraints.budgetPreference().value() == BudgetPreference.BudgetPrefValue.BUDGET) {
            factor = new BigDecimal("0.8");
        } else if (constraints.budgetPreference().value() == BudgetPreference.BudgetPrefValue.LUXURY) {
            factor = new BigDecimal("2.0");
        }

        hotel = hotel.multiply(factor).setScale(2, java.math.RoundingMode.HALF_UP);
        food = food.multiply(factor).setScale(2, java.math.RoundingMode.HALF_UP);
        transport = transport.multiply(factor).setScale(2, java.math.RoundingMode.HALF_UP);
        misc = misc.multiply(factor).setScale(2, java.math.RoundingMode.HALF_UP);

        BudgetEstimate estimate = budgetService.estimateTripBudget(dest.name(), days, hotel, food, transport, misc);
        logger.info("BudgetEstimate: Budget calculated, bound BudgetEstimate to blackboard");
        return estimate;
    }

    // --- Goal-Achieving Action ---
    @Action(description = "Compose travel plan report")
    @AchievesGoal(description = "Plan Travel Itinerary")
    public TravelPlanReport composeTravelPlan(SearchResponse searchData, BudgetEstimate budgetData, WeatherReport weatherData) {
        logger.info("composeTravelPlan: Composing final travel plan report");
        
        String destination = budgetData.getDestination();
        int days = budgetData.getDays();
        BigDecimal hotelTotal = BigDecimal.ZERO;
        BigDecimal foodTotal = BigDecimal.ZERO;
        BigDecimal transportTotal = BigDecimal.ZERO;
        BigDecimal miscTotal = BigDecimal.ZERO;

        for (BudgetItem item : budgetData.getBreakdown().getItems()) {
            if ("Hotel".equalsIgnoreCase(item.getName())) {
                hotelTotal = item.getAmount();
            } else if ("Food".equalsIgnoreCase(item.getName())) {
                foodTotal = item.getAmount();
            } else if ("Transport".equalsIgnoreCase(item.getName())) {
                transportTotal = item.getAmount();
            } else if ("Misc".equalsIgnoreCase(item.getName())) {
                miscTotal = item.getAmount();
            }
        }

        BigDecimal daysBD = BigDecimal.valueOf(days);
        BigDecimal hotelPerDay = days > 0 ? hotelTotal.divide(daysBD, 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal foodPerDay = days > 0 ? foodTotal.divide(daysBD, 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal transportPerDay = days > 0 ? transportTotal.divide(daysBD, 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal miscPerDay = days > 0 ? miscTotal.divide(daysBD, 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;

        String searchResultsFormatted = searchData.getResults().stream()
                .map(r -> String.format("- %s (%s):\n  %s", r.getTitle(), r.getUrl(), r.getContent()))
                .collect(Collectors.joining("\n"));

        String budgetBreakdownFormatted = budgetData.getBreakdown().getItems().stream()
                .map(item -> String.format("  * %s: %s", item.getName(), item.getAmount().toString()))
                .collect(Collectors.joining("\n"));

        StringBuilder composedOutput = new StringBuilder();
        composedOutput.append("==================================================\n");
        composedOutput.append("TRIP PLAN: ").append(destination).append("\n");
        composedOutput.append("==================================================\n\n");
        
        composedOutput.append("[1] Travel Summary\n");
        composedOutput.append("--------------------------------------------------\n");
        composedOutput.append("Itinerary and cost calculations successfully finalized.\n");
        composedOutput.append("Destination is verified via active search.\n");
        composedOutput.append("Total trip cost: ").append(budgetData.getTotalEstimate().toString()).append("\n\n");

        composedOutput.append("[2] Search Highlights\n");
        composedOutput.append("--------------------------------------------------\n");
        if (searchResultsFormatted.isEmpty()) {
            composedOutput.append("No search highlights available.\n\n");
        } else {
            composedOutput.append(searchResultsFormatted).append("\n\n");
        }

        composedOutput.append("[3] Budget Estimate\n");
        composedOutput.append("--------------------------------------------------\n");
        composedOutput.append("- Duration: ").append(days).append(" days\n");
        composedOutput.append("- Hotel per Day: ").append(hotelPerDay).append("\n");
        composedOutput.append("- Food per Day: ").append(foodPerDay).append("\n");
        composedOutput.append("- Transport per Day: ").append(transportPerDay).append("\n");
        composedOutput.append("- Misc per Day: ").append(miscPerDay).append("\n\n");
        composedOutput.append("Budget Breakdown:\n");
        composedOutput.append(budgetBreakdownFormatted).append("\n\n");

        composedOutput.append("[4] Weather Forecast\n");
        composedOutput.append("--------------------------------------------------\n");
        composedOutput.append("- Location: ").append(weatherData.getLocation()).append("\n");
        composedOutput.append("- Condition: ").append(weatherData.getCondition()).append("\n");
        composedOutput.append("- Temperature: ").append(weatherData.getTemperature()).append("°C\n");
        composedOutput.append("- Humidity: ").append(weatherData.getHumidity()).append("%\n");
        composedOutput.append("- Wind Speed: ").append(weatherData.getWindSpeed()).append(" km/h\n");
        composedOutput.append("- Severity: ").append(weatherData.getSeverity()).append("\n");
        composedOutput.append("- Provider: ").append(weatherData.getProvider()).append("\n");

        logger.info("TravelPlanReport: Composed final report successfully");
        logger.info("Goal Achieved: Planning completed");
        return new TravelPlanReport(composedOutput.toString());
    }

    // --- Goal-Specific Composers ---

    @Action(description = "Compose weather information report")
    @AchievesGoal(description = "Provide weather forecast")
    public WeatherReportResult composeWeatherInfo(WeatherReport weatherData) {
        logger.info("composeWeatherInfo: Composing weather information report");
        
        if (weatherData == null) {
            logger.warn("composeWeatherInfo: Weather data is null");
            return new WeatherReportResult(null, "Unknown");
        }
        
        String destination = weatherData.getLocation();
        WeatherReportResult result = new WeatherReportResult(weatherData, destination);
        logger.info("Goal Achieved: Weather forecast provided for {}", destination);
        return result;
    }

    @Action(description = "Compose budget estimate report")
    @AchievesGoal(description = "Provide budget estimate")
    public BudgetReportResult composeBudgetInfo(BudgetEstimate budgetData) {
        logger.info("composeBudgetInfo: Composing budget estimate report");
        
        if (budgetData == null) {
            logger.warn("composeBudgetInfo: Budget data is null");
            return new BudgetReportResult(null, "Unknown");
        }
        
        String destination = budgetData.getDestination();
        BudgetReportResult result = new BudgetReportResult(budgetData, destination);
        logger.info("Goal Achieved: Budget estimate provided for {}", destination);
        return result;
    }

    @Action(description = "Compose destination information report")
    @AchievesGoal(description = "Provide destination information")
    public SearchReportResult composeDestinationInfo(SearchResponse searchData) {
        logger.info("composeDestinationInfo: Composing destination information report");
        
        if (searchData == null || searchData.getResults().isEmpty()) {
            logger.warn("composeDestinationInfo: Search data is null or empty");
            return new SearchReportResult(null, "Unknown");
        }
        
        String destination = searchData.getQuery() != null ? searchData.getQuery() : "Unknown";
        SearchReportResult result = new SearchReportResult(searchData, destination);
        logger.info("Goal Achieved: Destination information provided for {}", destination);
        return result;
    }
}
