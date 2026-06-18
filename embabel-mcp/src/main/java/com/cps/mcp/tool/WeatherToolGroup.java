package com.cps.mcp.tool;

import com.embabel.agent.core.ToolGroup;
import com.embabel.agent.core.ToolGroupMetadata;
import com.embabel.agent.api.tool.Tool;
import com.cps.mcp.weather.model.WeatherReport;
import com.cps.mcp.weather.provider.WeatherProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;

/**
 * Weather Tool Group implementing Embabel's ToolGroup interface.
 * Exposes a weather lookup tool.
 */
@Component
public class WeatherToolGroup implements ToolGroup {

    private static final Logger logger = LoggerFactory.getLogger(WeatherToolGroup.class);
    private final ToolGroupMetadata metadata;
    private final WeatherProvider weatherProvider;

    public WeatherToolGroup(WeatherProvider weatherProvider) {
        this.weatherProvider = weatherProvider;

        this.metadata = (ToolGroupMetadata) Proxy.newProxyInstance(
                ToolGroupMetadata.class.getClassLoader(),
                new Class<?>[]{ToolGroupMetadata.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getRole":
                            return "weather";
                        case "getDescription":
                            return "Fetch real-time weather information and conditions";
                        case "getName":
                            return "WeatherToolGroup";
                        case "getProvider":
                            return "local";
                        case "getPermissions":
                            return Collections.emptySet();
                        case "infoString":
                            return "WeatherToolGroup 1.0.0";
                        case "toString":
                            return "WeatherToolGroup";
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
        Tool getWeatherTool = Tool.fromFunction(
                "get_weather",
                "Retrieves weather conditions and details for a given location",
                String.class,
                String.class,
                location -> {
                    logTraceStart("get_weather", location);
                    try {
                        WeatherReport report = weatherProvider.getWeather(location);
                        logTraceResult("get_weather", report);
                        String output = formatResponse(report);
                        logTraceCompleted("get_weather");
                        return output;
                    } catch (Exception e) {
                        logger.error("Weather Tool: Error getting weather for '{}': {}", location, e.getMessage());
                        throw new RuntimeException("Weather check failed: " + e.getMessage(), e);
                    }
                }
        );

        return List.of(getWeatherTool);
    }

    private void logTraceStart(String toolName, String location) {
        logger.info("Agent: Dispatching request to weather tool group");
        logger.info("Planner: Triggering weather tool function '{}'", toolName);
        logger.info("Weather Tool: Routing location '{}' to active provider", location);
        logger.info("Selected Provider: {}", weatherProvider.getName());
        logger.info("Weather Execution: Querying external weather API for '{}'", location);
    }

    private void logTraceResult(String toolName, WeatherReport report) {
        logger.info("Result: Retrieved weather condition '{}', temp {}°C from provider", report.getCondition(), report.getTemperature());
    }

    private void logTraceCompleted(String toolName) {
        logger.info("Goal Completed: Weather tool function '{}' execution successfully finished", toolName);
    }

    private String formatResponse(WeatherReport report) {
        return String.format(
                "Location: %s\nTemperature: %.1f°C\nCondition: %s\nHumidity: %.1f%%\nWind Speed: %.1f km/h\nTimestamp: %d\nProvider: %s\nSeverity: %s",
                report.getLocation(),
                report.getTemperature(),
                report.getCondition(),
                report.getHumidity(),
                report.getWindSpeed(),
                report.getTimestamp(),
                report.getProvider(),
                report.getSeverity()
        );
    }
}
