package com.cps.mcp.weather.provider;

import com.cps.mcp.weather.model.WeatherReport;
import com.cps.mcp.weather.model.WeatherReport.WeatherSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Open-Meteo API weather provider. Requires no API keys.
 */
@Component
public class OpenMeteoWeatherProvider implements WeatherProvider {

    private static final Logger logger = LoggerFactory.getLogger(OpenMeteoWeatherProvider.class);
    private final RestTemplate restTemplate;

    public OpenMeteoWeatherProvider() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5 seconds connect timeout
        factory.setReadTimeout(5000);    // 5 seconds read timeout
        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    public WeatherReport getWeather(String location) throws Exception {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location name is missing or blank");
        }

        try {
            // 1. Geocoding lookup
            String geocodeUrl = "https://geocoding-api.open-meteo.com/v1/search?name="
                    + URLEncoder.encode(location.trim(), StandardCharsets.UTF_8)
                    + "&count=1&language=en&format=json";

            logger.info("Open-Meteo Geocoding: Resolving location '{}'", location);
            Map<?, ?> geocodeResponse = restTemplate.getForObject(geocodeUrl, Map.class);

            if (geocodeResponse == null || !geocodeResponse.containsKey("results")) {
                throw new IllegalArgumentException("Unknown location: " + location);
            }
            List<?> results = (List<?>) geocodeResponse.get("results");
            if (results == null || results.isEmpty()) {
                throw new IllegalArgumentException("Unknown location: " + location);
            }

            Map<?, ?> firstResult = (Map<?, ?>) results.get(0);
            Number latNum = (Number) firstResult.get("latitude");
            Number lonNum = (Number) firstResult.get("longitude");
            String resolvedName = (String) firstResult.get("name");

            if (latNum == null || lonNum == null) {
                throw new RuntimeException("Malformed geocoding response for location: " + location);
            }

            double latitude = latNum.doubleValue();
            double longitude = lonNum.doubleValue();
            logger.info("Open-Meteo Geocoding: Resolved '{}' to lat={}, lon={}", location, latitude, longitude);

            // 2. Weather lookup
            String weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude
                    + "&longitude=" + longitude
                    + "&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m";

            logger.info("Open-Meteo Weather: Fetching forecast for lat={}, lon={}", latitude, longitude);
            Map<?, ?> weatherResponse = restTemplate.getForObject(weatherUrl, Map.class);

            if (weatherResponse == null || !weatherResponse.containsKey("current")) {
                throw new RuntimeException("Malformed weather forecast response for location: " + location);
            }

            Map<?, ?> current = (Map<?, ?>) weatherResponse.get("current");
            if (current == null) {
                throw new RuntimeException("Malformed weather current forecast for location: " + location);
            }

            Number tempNum = (Number) current.get("temperature_2m");
            Number humidityNum = (Number) current.get("relative_humidity_2m");
            Number windSpeedNum = (Number) current.get("wind_speed_10m");
            Number wmoCodeNum = (Number) current.get("weather_code");

            double temperature = tempNum != null ? tempNum.doubleValue() : 0.0;
            double humidity = humidityNum != null ? humidityNum.doubleValue() : 0.0;
            double windSpeed = windSpeedNum != null ? windSpeedNum.doubleValue() : 0.0;
            int wmoCode = wmoCodeNum != null ? wmoCodeNum.intValue() : 0;

            // Normalize WMO code
            String condition = mapWmoCodeToCondition(wmoCode);
            WeatherSeverity severity = mapWmoCodeToSeverity(wmoCode);
            long timestamp = System.currentTimeMillis();

            return new WeatherReport(
                    resolvedName != null ? resolvedName : location,
                    temperature,
                    condition,
                    humidity,
                    windSpeed,
                    timestamp,
                    "open-meteo",
                    severity
            );

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            throw new RuntimeException("Weather API HTTP client error (HTTP " + e.getStatusCode().value() + "): " + e.getResponseBodyAsString(), e);
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            throw new RuntimeException("Weather API HTTP server error (HTTP " + e.getStatusCode().value() + "): " + e.getResponseBodyAsString(), e);
        } catch (org.springframework.web.client.ResourceAccessException e) {
            throw new RuntimeException("Weather API request timed out or network unreachable: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Weather lookup failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String getName() {
        return "open-meteo";
    }

    private String mapWmoCodeToCondition(int code) {
        switch (code) {
            case 0: return "Sunny";
            case 1: return "Mostly Sunny";
            case 2: return "Partly Cloudy";
            case 3: return "Cloudy";
            case 45:
            case 48: return "Foggy";
            case 51:
            case 53:
            case 55: return "Drizzle";
            case 61: return "Rain";
            case 63:
            case 65: return "Heavy Rain";
            case 71:
            case 73:
            case 75: return "Snowy";
            case 77: return "Snow Grains";
            case 80:
            case 81:
            case 82: return "Rain Showers";
            case 85:
            case 86: return "Snow Showers";
            case 95: return "Thunderstorm";
            case 96:
            case 99: return "Severe Thunderstorm";
            default: return "Unknown (" + code + ")";
        }
    }

    private WeatherSeverity mapWmoCodeToSeverity(int code) {
        switch (code) {
            case 0:
            case 1:
            case 2:
                return WeatherSeverity.GOOD;
            case 3:
            case 45:
            case 48:
            case 51:
            case 53:
            case 55:
            case 80:
            case 81:
            case 82:
                return WeatherSeverity.MODERATE;
            case 61:
            case 63:
            case 65:
            case 71:
            case 73:
            case 75:
            case 77:
            case 85:
            case 86:
            case 95:
            case 96:
            case 99:
                return WeatherSeverity.POOR;
            default:
                return WeatherSeverity.MODERATE;
        }
    }
}
