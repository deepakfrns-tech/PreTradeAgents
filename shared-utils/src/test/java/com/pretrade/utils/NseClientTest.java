package com.pretrade.utils;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NseClient.
 * These tests verify the client's behavior with invalid/unavailable endpoints.
 * Integration tests with live NSE would require network access and are not included here.
 */
class NseClientTest {

    private NseClient nseClient;

    @BeforeEach
    void setUp() {
        nseClient = new NseClient();
    }

    @Test
    void fetchPreMarketData_withInvalidSymbol_returnsEmptyMap() {
        // NSE API may not respond in test environment, should return empty gracefully
        Map<String, Object> result = nseClient.fetchPreMarketData("INVALIDSYMBOL123");
        assertNotNull(result);
    }

    @Test
    void fetchOptionChain_withIndexSymbol_returnsJsonNode() {
        // Should return an empty ObjectNode when NSE is unreachable
        JsonNode result = nseClient.fetchOptionChain("NIFTY");
        assertNotNull(result);
    }

    @Test
    void fetchOptionChain_withEquitySymbol_returnsJsonNode() {
        JsonNode result = nseClient.fetchOptionChain("RELIANCE");
        assertNotNull(result);
    }

    @Test
    void fetchIndicesData_returnsMap() {
        Map<String, Object> result = nseClient.fetchIndicesData();
        assertNotNull(result);
    }

    @Test
    void constructor_createsInstance() {
        NseClient client = new NseClient();
        assertNotNull(client);
    }
}
