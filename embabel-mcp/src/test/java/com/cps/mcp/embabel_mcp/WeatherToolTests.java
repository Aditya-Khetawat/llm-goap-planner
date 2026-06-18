package com.cps.mcp.embabel_mcp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;

import com.embabel.agent.core.ToolGroup;
import com.embabel.agent.spi.ToolGroupResolver;
import com.embabel.agent.spi.support.RegistryToolGroupResolver;
import com.embabel.agent.api.tool.Tool;
import com.cps.mcp.tool.WeatherToolGroup;
import com.cps.mcp.weather.model.WeatherReport;
import com.cps.mcp.weather.model.WeatherReport.WeatherSeverity;
import com.cps.mcp.weather.provider.WeatherProvider;
import com.cps.mcp.weather.provider.OpenMeteoWeatherProvider;

import java.util.List;
import java.util.Map;

@SpringBootTest
public class WeatherToolTests {

    private static final Logger logger = LoggerFactory.getLogger(WeatherToolTests.class);

    @Autowired
    private WeatherToolGroup weatherToolGroup;

    @Autowired
    private ToolGroupResolver toolGroupResolver;

    @Test
    public void testToolRegistration() {
        assertNotNull(weatherToolGroup, "WeatherToolGroup bean should be registered in the Spring Context");
    }

    @Test
    public void testToolDiscovery() {
        assertNotNull(toolGroupResolver);
        assertTrue(toolGroupResolver instanceof RegistryToolGroupResolver);
        List<ToolGroup> groups = ((RegistryToolGroupResolver) toolGroupResolver).getToolGroups();
        boolean found = groups.stream()
                .anyMatch(tg -> "WeatherToolGroup".equals(tg.getMetadata().getName()));
        assertTrue(found, "WeatherToolGroup should be discovered by the ToolGroupResolver");
    }

    @Test
    public void testSuccessfulWeatherLookupMock() throws Exception {
        WeatherProvider mockProvider = mock(WeatherProvider.class);
        when(mockProvider.getName()).thenReturn("mock-provider");
        
        WeatherReport mockReport = new WeatherReport(
                "Jaipur", 30.5, "Sunny", 45.0, 12.0,
                1718123456789L, "mock-provider", WeatherSeverity.GOOD
        );
        when(mockProvider.getWeather(eq("Jaipur"))).thenReturn(mockReport);

        WeatherToolGroup testToolGroup = new WeatherToolGroup(mockProvider);
        List<Tool> tools = testToolGroup.getTools();
        assertEquals(1, tools.size());

        Tool weatherTool = tools.get(0);
        assertEquals("get_weather", weatherTool.getDefinition().getName());

        // Invoke tool
        Tool.Result result = weatherTool.call("\"Jaipur\"");
        assertNotNull(result);
        assertTrue(result instanceof Tool.Result.Text);

        String content = ((Tool.Result.Text) result).getContent();
        assertTrue(content.contains("Location: Jaipur"));
        assertTrue(content.contains("Temperature: 30.5°C"));
        assertTrue(content.contains("Condition: Sunny"));
        assertTrue(content.contains("Severity: GOOD"));
        assertTrue(content.contains("Provider: mock-provider"));

        verify(mockProvider, times(1)).getWeather("Jaipur");
    }

    @Test
    public void testOpenMeteoProviderMappingAndNormalization() throws Exception {
        OpenMeteoWeatherProvider provider = new OpenMeteoWeatherProvider();
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(provider, "restTemplate", mockRestTemplate);

        // Geocoding mock
        Map<String, Object> mockGeocodeResponse = Map.of(
                "results", List.of(
                        Map.of("name", "Prague", "latitude", 50.088, "longitude", 14.4207)
                )
        );
        when(mockRestTemplate.getForObject(contains("geocoding-api.open-meteo.com"), eq(Map.class)))
                .thenReturn(mockGeocodeResponse);

        // Weather mock for code 95 (Thunderstorm -> POOR severity)
        Map<String, Object> mockWeatherResponse = Map.of(
                "current", Map.of(
                        "temperature_2m", 18.2,
                        "relative_humidity_2m", 88.0,
                        "wind_speed_10m", 25.4,
                        "weather_code", 95
                )
        );
        when(mockRestTemplate.getForObject(contains("api.open-meteo.com/v1/forecast"), eq(Map.class)))
                .thenReturn(mockWeatherResponse);

        WeatherReport report = provider.getWeather("Prague");
        assertNotNull(report);
        assertEquals("Prague", report.getLocation());
        assertEquals(18.2, report.getTemperature());
        assertEquals("Thunderstorm", report.getCondition());
        assertEquals(88.0, report.getHumidity());
        assertEquals(25.4, report.getWindSpeed());
        assertEquals("open-meteo", report.getProvider());
        assertEquals(WeatherSeverity.POOR, report.getSeverity());

        // Weather mock for code 1 (Mostly Sunny -> GOOD severity)
        Map<String, Object> mockWeatherResponseGood = Map.of(
                "current", Map.of(
                        "temperature_2m", 25.0,
                        "relative_humidity_2m", 40.0,
                        "wind_speed_10m", 8.0,
                        "weather_code", 1
                )
        );
        when(mockRestTemplate.getForObject(contains("api.open-meteo.com/v1/forecast"), eq(Map.class)))
                .thenReturn(mockWeatherResponseGood);

        WeatherReport reportGood = provider.getWeather("Prague");
        assertNotNull(reportGood);
        assertEquals("Mostly Sunny", reportGood.getCondition());
        assertEquals(WeatherSeverity.GOOD, reportGood.getSeverity());
    }

    @Test
    public void testOpenMeteoProviderUnknownLocation() {
        OpenMeteoWeatherProvider provider = new OpenMeteoWeatherProvider();
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(provider, "restTemplate", mockRestTemplate);

        // Geocoding returns empty results list
        Map<String, Object> mockGeocodeResponse = Map.of("results", List.of());
        when(mockRestTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(mockGeocodeResponse);

        assertThrows(IllegalArgumentException.class, () -> provider.getWeather("InvalidCity"));
    }

    @Test
    public void testOpenMeteoTimeoutHandling() {
        OpenMeteoWeatherProvider provider = new OpenMeteoWeatherProvider();
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(provider, "restTemplate", mockRestTemplate);

        when(mockRestTemplate.getForObject(anyString(), eq(Map.class)))
                .thenThrow(new ResourceAccessException("Connection timed out"));

        Exception ex = assertThrows(RuntimeException.class, () -> provider.getWeather("Prague"));
        assertTrue(ex.getMessage().contains("timed out or network unreachable"));
    }

    @Test
    public void testOpenMeteoErrorPropagation() {
        OpenMeteoWeatherProvider provider = new OpenMeteoWeatherProvider();
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(provider, "restTemplate", mockRestTemplate);

        when(mockRestTemplate.getForObject(anyString(), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"));

        Exception ex = assertThrows(RuntimeException.class, () -> provider.getWeather("Prague"));
        assertTrue(ex.getMessage().contains("HTTP 500"));
    }
}
