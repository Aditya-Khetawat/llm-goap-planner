package com.cps.mcp.weather.model;

/**
 * A provider-agnostic weather report wrapper.
 * Placed on the blackboard for planner consumption.
 */
public class WeatherReport {

    public enum WeatherSeverity {
        GOOD, MODERATE, POOR
    }

    private final String location;
    private final double temperature;
    private final String condition;
    private final double humidity;
    private final double windSpeed;
    private final long timestamp;
    private final String provider;
    private final WeatherSeverity severity;

    public WeatherReport(String location, double temperature, String condition, double humidity,
                         double windSpeed, long timestamp, String provider, WeatherSeverity severity) {
        this.location = location;
        this.temperature = temperature;
        this.condition = condition;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.timestamp = timestamp;
        this.provider = provider;
        this.severity = severity;
    }

    public String getLocation() {
        return location;
    }

    public double getTemperature() {
        return temperature;
    }

    public String getCondition() {
        return condition;
    }

    public double getHumidity() {
        return humidity;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getProvider() {
        return provider;
    }

    public WeatherSeverity getSeverity() {
        return severity;
    }

    @Override
    public String toString() {
        return "WeatherReport{" +
                "location='" + location + '\'' +
                ", temperature=" + temperature +
                ", condition='" + condition + '\'' +
                ", humidity=" + humidity +
                ", windSpeed=" + windSpeed +
                ", timestamp=" + timestamp +
                ", provider='" + provider + '\'' +
                ", severity=" + severity +
                '}';
    }
}
