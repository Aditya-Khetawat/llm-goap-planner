package com.cps.mcp.embabel_mcp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.cps.mcp.search.model.SearchResponse;
import com.cps.mcp.search.model.SearchResult;
import com.cps.mcp.search.provider.TavilySearchProvider;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

public class TavilySearchProviderTests {

    @Test
    public void testApiKeyValidation() {
        assertThrows(IllegalArgumentException.class, () -> new TavilySearchProvider(null));
        assertThrows(IllegalArgumentException.class, () -> new TavilySearchProvider("   "));
    }

    @Test
    public void testSuccessfulResponseMappingAndNormalization() throws Exception {
        TavilySearchProvider provider = new TavilySearchProvider("test-key", 2);

        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(provider, "restTemplate", mockRestTemplate);

        Map<String, Object> mockResponse = Map.of(
                "results", List.of(
                        Map.of("title", " Prague Attractions  ", "url", "https://prague.com", "content", "A beautiful city"),
                        Map.of("title", "Duplicate URL", "url", "https://prague.com", "content", "Ignored duplicate"),
                        Map.of("title", "  ", "url", "https://empty-title.com", "content", "Ignored empty title"),
                        Map.of("title", "Valid Two", "url", "https://valid2.com", "content", "Content two"),
                        Map.of("title", "Valid Three (Exceeds Limit)", "url", "https://valid3.com", "content", "Ignored by limit")
                )
        );

        when(mockRestTemplate.postForObject(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        SearchResponse response = provider.search("Prague");

        assertNotNull(response);
        assertEquals("tavily", response.getProvider());
        assertEquals("Prague", response.getQuery());
        assertEquals(2, response.getResultCount());
        assertNotNull(response.getTimestamp());

        List<SearchResult> results = response.getResults();
        assertEquals(2, results.size());

        // Trimmed title
        assertEquals("Prague Attractions", results.get(0).getTitle());
        assertEquals("https://prague.com", results.get(0).getUrl());
        assertEquals("A beautiful city", results.get(0).getContent());

        // Ignored duplicate URL & empty title -> second valid is "Valid Two"
        assertEquals("Valid Two", results.get(1).getTitle());
        assertEquals("https://valid2.com", results.get(1).getUrl());
    }

    @Test
    public void testRetryOn429ThenSuccess() throws Exception {
        TavilySearchProvider provider = new TavilySearchProvider("test-key", 5);

        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(provider, "restTemplate", mockRestTemplate);

        Map<String, Object> mockResponse = Map.of(
                "results", List.of(Map.of("title", "Success", "url", "https://ok.com", "content", "ok"))
        );

        // First call throws 429, second call succeeds
        when(mockRestTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS, "Rate limit"))
                .thenReturn(mockResponse);

        SearchResponse response = provider.search("Prague");
        assertNotNull(response);
        assertEquals(1, response.getResults().size());
        assertEquals("Success", response.getResults().get(0).getTitle());

        verify(mockRestTemplate, times(2)).postForObject(anyString(), any(), eq(Map.class));
    }

    @Test
    public void testRetryOn429Exhausted() {
        TavilySearchProvider provider = new TavilySearchProvider("test-key", 5);

        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(provider, "restTemplate", mockRestTemplate);

        // All calls throw 429
        when(mockRestTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS, "Rate limit"));

        Exception exception = assertThrows(RuntimeException.class, () -> provider.search("Prague"));
        assertTrue(exception.getMessage().contains("Tavily search failed (HTTP 429)"));

        // Max 1 retry means total 2 calls (initial + 1 retry)
        verify(mockRestTemplate, times(2)).postForObject(anyString(), any(), eq(Map.class));
    }

    @Test
    public void testNoRetryOn401Unauthorized() {
        TavilySearchProvider provider = new TavilySearchProvider("test-key", 5);

        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(provider, "restTemplate", mockRestTemplate);

        // 401 Unauthorized
        when(mockRestTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

        Exception exception = assertThrows(RuntimeException.class, () -> provider.search("Prague"));
        assertTrue(exception.getMessage().contains("Tavily authentication failed: Invalid API key"));

        // No retries for Auth failures -> only 1 call
        verify(mockRestTemplate, times(1)).postForObject(anyString(), any(), eq(Map.class));
    }

    @Test
    public void testRetryOnTimeoutThenSuccess() throws Exception {
        TavilySearchProvider provider = new TavilySearchProvider("test-key", 5);

        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(provider, "restTemplate", mockRestTemplate);

        Map<String, Object> mockResponse = Map.of(
                "results", List.of(Map.of("title", "Success", "url", "https://ok.com", "content", "ok"))
        );

        // First call times out, second succeeds
        when(mockRestTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenThrow(new ResourceAccessException("Timeout"))
                .thenReturn(mockResponse);

        SearchResponse response = provider.search("Prague");
        assertNotNull(response);
        assertEquals("Success", response.getResults().get(0).getTitle());

        verify(mockRestTemplate, times(2)).postForObject(anyString(), any(), eq(Map.class));
    }
}
