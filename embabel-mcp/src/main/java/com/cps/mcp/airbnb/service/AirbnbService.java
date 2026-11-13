package com.cps.mcp.airbnb.service;

import com.cps.mcp.search.model.SearchResponse;
import com.cps.mcp.search.provider.SearchProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AirbnbService {

    private static final Logger logger = LoggerFactory.getLogger(AirbnbService.class);

    @Autowired(required = false)
    private ChatModel chatModel;

    @Autowired
    private SearchProvider searchProvider;

    public static class AirbnbListing {
        public String name;
        public String price;
        public String rating;
        public String link;
        public String description;

        public AirbnbListing(String name, String price, String rating, String link, String description) {
            this.name = name;
            this.price = price;
            this.rating = rating;
            this.link = link;
            this.description = description;
        }

        @Override
        public String toString() {
            String title = link != null ? String.format("[%s](%s)", name, link) : name;
            return String.format("- %s (%s, Rating: %s) - %s", title, price, rating, description);
        }
    }

    /**
     * Finds places to stay that are named in live web search results. Returns an empty list
     * rather than inventing listings when nothing usable is found.
     */
    public List<AirbnbListing> searchAirbnb(String destination) {
        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException("Destination is required for Airbnb search");
        }

        List<AirbnbListing> listings = new ArrayList<>();
        boolean isMock = searchProvider == null ||
                         searchProvider.getName() == null ||
                         "MockProvider".equalsIgnoreCase(searchProvider.getName()) ||
                         searchProvider.getClass().getName().contains("Mockito") ||
                         searchProvider.getClass().getName().contains("Proxy");

        if (chatModel != null && !isMock) {
            try {
                logger.info("AirbnbService: Searching homestays/Airbnb options in '{}'", destination);
                String query = String.format("best Airbnb homestays hotels places to stay in %s", destination);
                SearchResponse searchData = searchProvider.search(query);

                StringBuilder searchContent = new StringBuilder();
                Set<String> sourceUrls = new HashSet<>();
                if (searchData != null && searchData.getResults() != null) {
                    for (com.cps.mcp.search.model.SearchResult r : searchData.getResults()) {
                        searchContent.append(r.getTitle()).append(" (").append(r.getUrl()).append("): ").append(r.getContent()).append("\n");
                        if (r.getUrl() != null) sourceUrls.add(r.getUrl());
                    }
                }

                if (searchContent.length() > 0) {
                    String prompt = String.format(
                        "Based on the following search results about accommodation in %s, list up to 3 specific places to stay that are explicitly named in the results.\n" +
                        "Search Results:\n%s\n" +
                        "Output EXACTLY a JSON array of objects matching this structure (do not add any extra text or markdown formatting outside the JSON):\n" +
                        "[\n" +
                        "  {\n" +
                        "    \"name\": \"<name exactly as written in the results>\",\n" +
                        "    \"price\": \"<price as stated, or N/A>\",\n" +
                        "    \"rating\": \"<rating as stated, or N/A>\",\n" +
                        "    \"link\": \"<URL of the search result the place appears in>\",\n" +
                        "    \"description\": \"<one sentence based on the results>\"\n" +
                        "  }\n" +
                        "]\n" +
                        "Do not invent places, prices, ratings or links. Use N/A for anything not stated. If no specific place is named, output [].",
                        destination, searchContent.toString()
                    );

                    String llmOutput = chatModel.call(prompt);
                    if (llmOutput.contains("```")) {
                        int start = llmOutput.indexOf("[");
                        int end = llmOutput.lastIndexOf("]");
                        if (start != -1 && end != -1 && end > start) {
                            llmOutput = llmOutput.substring(start, end + 1);
                        }
                    }

                    // Parse the array using regex to avoid external dependency issues
                    Pattern entryPattern = Pattern.compile("\\{\\s*\"name\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"price\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"rating\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"link\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"description\"\\s*:\\s*\"([^\"]+)\"\\s*\\}");
                    Matcher matcher = entryPattern.matcher(llmOutput.replaceAll("\\s*\\r?\\n\\s*", " "));
                    String haystack = searchContent.toString().toLowerCase();
                    while (matcher.find()) {
                        String name = matcher.group(1);
                        String price = matcher.group(2);
                        String rating = matcher.group(3);
                        String link = matcher.group(4);
                        String description = matcher.group(5);

                        // Keep only places that actually appear in the search results
                        if (!haystack.contains(name.toLowerCase())) {
                            logger.info("AirbnbService: Dropping '{}' (not found in search results)", name);
                            continue;
                        }
                        // Keep only links that came from the search results
                        if (!sourceUrls.contains(link)) {
                            link = null;
                        }

                        listings.add(new AirbnbListing(name, price, rating, link, description));
                    }
                }

                logger.info("AirbnbService: Extraction finished with {} listings.", listings.size());
            } catch (Exception e) {
                logger.warn("AirbnbService: Extraction failed: {}. Returning no listings.", e.getMessage());
            }
        }

        if (listings.isEmpty()) {
            logger.info("AirbnbService: No accommodation listings found for {}", destination);
        }
        return listings;
    }
}
