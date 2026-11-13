package com.cps.mcp.agent;

import com.cps.mcp.weather.model.WeatherReport;

/**
 * Result DTO for weather-only queries.
 * Wraps weather forecast information for user-facing output.
 */
public class WeatherReportResult {
    private final WeatherReport report;
    private final String destination;

    public WeatherReportResult(WeatherReport report, String destination) {
        this.report = report;
        this.destination = destination;
    }

    public WeatherReport getReport() {
        return report;
    }

    public String getDestination() {
        return destination;
    }

    public String formatContent() {
        if (report == null) {
            return "Unable to retrieve weather information.";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("==================================================\n");
        sb.append("WEATHER FORECAST: ").append(destination).append("\n");
        sb.append("==================================================\n\n");
        sb.append("Location: ").append(report.getLocation()).append("\n");
        sb.append("Condition: ").append(report.getCondition()).append("\n");
        sb.append("Temperature: ").append(WeatherReport.formatMeasure(report.getTemperature(), "%.1f°C")).append("\n");
        sb.append("Humidity: ").append(WeatherReport.formatMeasure(report.getHumidity(), "%.0f%%")).append("\n");
        sb.append("Wind Speed: ").append(WeatherReport.formatMeasure(report.getWindSpeed(), "%.1f km/h")).append("\n");
        sb.append("Weather Severity: ").append(report.getSeverity()).append("\n");
        sb.append("Provider: ").append(report.getProvider()).append("\n");
        
        return sb.toString();
    }

    @Override
    public String toString() {
        return formatContent();
    }
}
