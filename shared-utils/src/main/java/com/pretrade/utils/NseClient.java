package com.pretrade.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Utility class for making NSE (National Stock Exchange) API calls.
 * Handles NSE-specific headers and retry logic with exponential backoff.
 */
@Slf4j
@Component
public class NseClient {

    private static final String NSE_BASE_URL = "https://www.nseindia.com";
    private static final String NSE_API_BASE = "https://www.nseindia.com/api";
    private static final String PRE_MARKET_URL = NSE_API_BASE + "/market-data-pre-open?key=FO";
    private static final String OPTION_CHAIN_URL = NSE_API_BASE + "/option-chain-indices?symbol=";
    private static final String EQUITY_OPTION_CHAIN_URL = NSE_API_BASE + "/option-chain-equities?symbol=";
    private static final String INDICES_URL = NSE_API_BASE + "/allIndices";

    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_BACKOFF_MS = 1000;
    private static final double BACKOFF_MULTIPLIER = 2.0;

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public NseClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Fetches pre-market data for a given symbol from NSE.
     *
     * @param symbol the stock/index symbol (e.g., "RELIANCE", "NIFTY")
     * @return pre-market data as a Map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> fetchPreMarketData(String symbol) {
        log.info("Fetching pre-market data for symbol: {}", symbol);
        String url = PRE_MARKET_URL;
        JsonNode response = executeWithRetry(url);
        if (response == null) {
            log.warn("No pre-market data returned for symbol: {}", symbol);
            return Map.of();
        }
        try {
            // Filter for the requested symbol from the full pre-market response
            JsonNode data = response.path("data");
            if (data.isArray()) {
                for (JsonNode entry : data) {
                    JsonNode metadata = entry.path("metadata");
                    if (metadata.has("symbol") && metadata.get("symbol").asText().equalsIgnoreCase(symbol)) {
                        return objectMapper.convertValue(entry, Map.class);
                    }
                }
            }
            return objectMapper.convertValue(response, Map.class);
        } catch (Exception e) {
            log.error("Error parsing pre-market data for symbol: {}", symbol, e);
            return Map.of();
        }
    }

    /**
     * Fetches the option chain for a given symbol from NSE.
     *
     * @param symbol the stock/index symbol (e.g., "NIFTY", "BANKNIFTY", "RELIANCE")
     * @return option chain data as a JsonNode
     */
    public JsonNode fetchOptionChain(String symbol) {
        log.info("Fetching option chain for symbol: {}", symbol);
        String url;
        if (isIndexSymbol(symbol)) {
            url = OPTION_CHAIN_URL + symbol;
        } else {
            url = EQUITY_OPTION_CHAIN_URL + symbol;
        }
        JsonNode response = executeWithRetry(url);
        if (response == null) {
            log.warn("No option chain data returned for symbol: {}", symbol);
            return objectMapper.createObjectNode();
        }
        return response;
    }

    /**
     * Fetches data for all NSE indices.
     *
     * @return indices data as a Map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> fetchIndicesData() {
        log.info("Fetching all indices data");
        JsonNode response = executeWithRetry(INDICES_URL);
        if (response == null) {
            log.warn("No indices data returned");
            return Map.of();
        }
        try {
            return objectMapper.convertValue(response, Map.class);
        } catch (Exception e) {
            log.error("Error parsing indices data", e);
            return Map.of();
        }
    }

    /**
     * Executes an HTTP GET request to NSE with retry logic and exponential backoff.
     *
     * @param url the URL to call
     * @return the response as a JsonNode, or null if all retries fail
     */
    private JsonNode executeWithRetry(String url) {
        long backoffMs = INITIAL_BACKOFF_MS;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                // First, hit the base URL to establish a session/cookie
                if (attempt == 1) {
                    warmUpSession();
                }

                HttpHeaders headers = buildNseHeaders();
                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(
                        url, HttpMethod.GET, entity, String.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    log.debug("Successfully fetched data from {} on attempt {}", url, attempt);
                    return objectMapper.readTree(response.getBody());
                }

                log.warn("Non-successful response from {} on attempt {}: status={}",
                        url, attempt, response.getStatusCode());

            } catch (RestClientException e) {
                log.warn("Request to {} failed on attempt {}/{}: {}",
                        url, attempt, MAX_RETRIES, e.getMessage());
            } catch (Exception e) {
                log.error("Unexpected error fetching from {} on attempt {}/{}: {}",
                        url, attempt, MAX_RETRIES, e.getMessage());
            }

            if (attempt < MAX_RETRIES) {
                log.info("Retrying in {} ms (attempt {}/{})", backoffMs, attempt + 1, MAX_RETRIES);
                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.warn("Retry sleep interrupted");
                    return null;
                }
                backoffMs = (long) (backoffMs * BACKOFF_MULTIPLIER);
            }
        }

        log.error("All {} retry attempts exhausted for URL: {}", MAX_RETRIES, url);
        return null;
    }

    /**
     * Warms up the NSE session by hitting the base URL first.
     * NSE requires an initial page load before API calls work.
     */
    private void warmUpSession() {
        try {
            HttpHeaders headers = buildNseHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            restTemplate.exchange(NSE_BASE_URL, HttpMethod.GET, entity, String.class);
        } catch (Exception e) {
            log.debug("Session warm-up request completed (errors expected): {}", e.getMessage());
        }
    }

    /**
     * Builds HTTP headers required for NSE API calls.
     */
    private HttpHeaders buildNseHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", USER_AGENT);
        headers.set("Accept", "application/json, text/plain, */*");
        headers.set("Accept-Language", "en-US,en;q=0.9");
        headers.set("Accept-Encoding", "gzip, deflate, br");
        headers.set("Referer", "https://www.nseindia.com/");
        headers.set("Connection", "keep-alive");
        headers.set("Cache-Control", "no-cache");
        headers.set("Pragma", "no-cache");
        return headers;
    }

    /**
     * Determines if a symbol is an index (NIFTY, BANKNIFTY, etc.) or an equity.
     */
    private boolean isIndexSymbol(String symbol) {
        return symbol != null && (
                symbol.equalsIgnoreCase("NIFTY") ||
                symbol.equalsIgnoreCase("BANKNIFTY") ||
                symbol.equalsIgnoreCase("FINNIFTY") ||
                symbol.equalsIgnoreCase("MIDCPNIFTY") ||
                symbol.equalsIgnoreCase("NIFTY 50") ||
                symbol.equalsIgnoreCase("NIFTY BANK"));
    }
}
