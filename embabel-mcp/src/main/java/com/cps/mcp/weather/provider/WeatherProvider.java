package com.cps.mcp.weather.provider;

import com.cps.mcp.weather.model.WeatherReport;

/**
 * Interface for weather service providers.
 */
public interface WeatherProvider {

    /**
     * Resolves location and retrieves current weather forecast information.
     *
     * @param location the location name to query
     * @return the structured WeatherReport
     * @throws Exception if lookup fails due to timeouts, invalid location, or HTTP errors
     */
    WeatherReport getWeather(String location) throws Exception;

    /**
     * Returns the name of the weather provider.
     */
    String getName();
}
