package com.pretrade.analyst.collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pretrade.analyst.config.AnalystSettings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class NseCollector {

    private static final String NSE_BASE_URL = "https://www.nseindia.com";
    private static final String NSE_PRE_MARKET_URL = "/api/market-data-pre-open?key=NIFTY";
    private static final String NSE_OPTION_CHAIN_URL = "/api/option-chain-indices?symbol=";
    private static final String NSE_MARKET_STATUS_URL = "/api/marketStatus";
    private static final String NSE_INDEX_URL = "/api/allIndices";

    private final AnalystSettings settings;
    private final ObjectMapper objectMapper;

    private WebClient buildNseClient() {
        return WebClient.builder()
                .baseUrl(NSE_BASE_URL)
                .defaultHeader(HttpHeaders.USER_AGENT,
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Accept-Language", "en-US,en;q=0.9")
                .defaultHeader("Accept-Encoding", "gzip, deflate, br")
                .build();
    }

    /**
     * Fetches pre-market data for NIFTY 50 constituents.
     * Returns a list of pre-market stock entries with gap, volume, and price data.
     */
    public List<PreMarketEntry> collectPreMarketData() {
        log.info("Collecting NSE pre-market data...");
        try {
            WebClient client = buildNseClient();
            // First hit the main page to get session cookies
            client.get().uri("/")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(settings.getDataSourceTimeoutSeconds()))
                    .block();

            String response = client.get()
                    .uri(NSE_PRE_MARKET_URL)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(settings.getDataSourceTimeoutSeconds()))
                    .block();

            return parsePreMarketResponse(response);
        } catch (Exception e) {
            log.error("Failed to collect NSE pre-market data: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Fetches the full option chain for a given symbol (e.g., NIFTY, BANKNIFTY, or stock symbols).
     */
    public OptionChainData collectOptionChain(String symbol) {
        log.info("Collecting option chain for symbol: {}", symbol);
        try {
            WebClient client = buildNseClient();
            client.get().uri("/")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(settings.getDataSourceTimeoutSeconds()))
                    .block();

            String response = client.get()
                    .uri(NSE_OPTION_CHAIN_URL + symbol)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(settings.getDataSourceTimeoutSeconds()))
                    .block();

            return parseOptionChainResponse(response, symbol);
        } catch (Exception e) {
            log.error("Failed to collect option chain for {}: {}", symbol, e.getMessage(), e);
            return OptionChainData.builder().symbol(symbol).entries(Collections.emptyList()).build();
        }
    }

    /**
     * Fetches broad market snapshot: index values, VIX, advance/decline.
     */
    public MarketSnapshotData collectMarketSnapshot() {
        log.info("Collecting market snapshot...");
        try {
            WebClient client = buildNseClient();
            client.get().uri("/")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(settings.getDataSourceTimeoutSeconds()))
                    .block();

            String indexResponse = client.get()
                    .uri(NSE_INDEX_URL)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(settings.getDataSourceTimeoutSeconds()))
                    .block();

            return parseMarketSnapshotResponse(indexResponse);
        } catch (Exception e) {
            log.error("Failed to collect market snapshot: {}", e.getMessage(), e);
            return MarketSnapshotData.builder().build();
        }
    }

    private List<PreMarketEntry> parsePreMarketResponse(String response) {
        List<PreMarketEntry> entries = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode dataArray = root.path("data");
            if (dataArray.isArray()) {
                for (JsonNode item : dataArray) {
                    JsonNode metadata = item.path("metadata");
                    entries.add(PreMarketEntry.builder()
                            .symbol(metadata.path("symbol").asText())
                            .previousClose(metadata.path("previousClose").asDouble())
                            .iep(metadata.path("iep").asDouble())
                            .change(metadata.path("change").asDouble())
                            .changePercent(metadata.path("pChange").asDouble())
                            .finalQuantity(metadata.path("finalQuantity").asLong())
                            .totalBuyQuantity(item.path("detail").path("preOpenMarket").path("totalBuyQuantity").asLong())
                            .totalSellQuantity(item.path("detail").path("preOpenMarket").path("totalSellQuantity").asLong())
                            .lastPrice(metadata.path("lastPrice").asDouble())
                            .yearHigh(metadata.path("yearHigh").asDouble())
                            .yearLow(metadata.path("yearLow").asDouble())
                            .build());
                }
            }
        } catch (Exception e) {
            log.error("Error parsing pre-market response: {}", e.getMessage(), e);
        }
        return entries;
    }

    private OptionChainData parseOptionChainResponse(String response, String symbol) {
        OptionChainData data = OptionChainData.builder()
                .symbol(symbol)
                .entries(new ArrayList<>())
                .build();
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode records = root.path("records");
            data.setUnderlyingValue(records.path("underlyingValue").asDouble());
            data.setExpiryDates(objectMapper.convertValue(
                    records.path("expiryDates"), new TypeReference<List<String>>() {}));

            JsonNode dataArray = records.path("data");
            if (dataArray.isArray()) {
                for (JsonNode item : dataArray) {
                    OptionEntry entry = new OptionEntry();
                    entry.setStrikePrice(item.path("strikePrice").asDouble());
                    entry.setExpiryDate(item.path("expiryDate").asText());

                    if (item.has("CE")) {
                        JsonNode ce = item.path("CE");
                        entry.setCallOI(ce.path("openInterest").asLong());
                        entry.setCallChangeInOI(ce.path("changeinOpenInterest").asLong());
                        entry.setCallLTP(ce.path("lastPrice").asDouble());
                        entry.setCallIV(ce.path("impliedVolatility").asDouble());
                        entry.setCallVolume(ce.path("totalTradedVolume").asLong());
                    }
                    if (item.has("PE")) {
                        JsonNode pe = item.path("PE");
                        entry.setPutOI(pe.path("openInterest").asLong());
                        entry.setPutChangeInOI(pe.path("changeinOpenInterest").asLong());
                        entry.setPutLTP(pe.path("lastPrice").asDouble());
                        entry.setPutIV(pe.path("impliedVolatility").asDouble());
                        entry.setPutVolume(pe.path("totalTradedVolume").asLong());
                    }
                    data.getEntries().add(entry);
                }
            }
        } catch (Exception e) {
            log.error("Error parsing option chain response for {}: {}", symbol, e.getMessage(), e);
        }
        return data;
    }

    private MarketSnapshotData parseMarketSnapshotResponse(String indexResponse) {
        MarketSnapshotData snapshot = MarketSnapshotData.builder().build();
        try {
            JsonNode root = objectMapper.readTree(indexResponse);
            JsonNode dataArray = root.path("data");
            if (dataArray.isArray()) {
                for (JsonNode item : dataArray) {
                    String indexName = item.path("index").asText();
                    switch (indexName) {
                        case "NIFTY 50" -> {
                            snapshot.setNiftyValue(item.path("last").asDouble());
                            snapshot.setNiftyChange(item.path("percentChange").asDouble());
                            snapshot.setAdvances(item.path("advances").asInt());
                            snapshot.setDeclines(item.path("declines").asInt());
                        }
                        case "NIFTY BANK" -> {
                            snapshot.setBankNiftyValue(item.path("last").asDouble());
                            snapshot.setBankNiftyChange(item.path("percentChange").asDouble());
                        }
                        case "INDIA VIX" -> snapshot.setIndiaVix(item.path("last").asDouble());
                    }
                }
            }
            if (snapshot.getAdvances() > 0 || snapshot.getDeclines() > 0) {
                snapshot.setAdvanceDeclineRatio(
                        (double) snapshot.getAdvances() / Math.max(1, snapshot.getDeclines()));
            }
        } catch (Exception e) {
            log.error("Error parsing market snapshot: {}", e.getMessage(), e);
        }
        return snapshot;
    }

    // --- DTOs ---

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PreMarketEntry {
        private String symbol;
        private double previousClose;
        private double iep; // Indicative Equilibrium Price
        private double change;
        private double changePercent;
        private long finalQuantity;
        private long totalBuyQuantity;
        private long totalSellQuantity;
        private double lastPrice;
        private double yearHigh;
        private double yearLow;

        public double getGapPercent() {
            if (previousClose == 0) return 0;
            return ((iep - previousClose) / previousClose) * 100.0;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionChainData {
        private String symbol;
        private double underlyingValue;
        private List<String> expiryDates;
        private List<OptionEntry> entries;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionEntry {
        private double strikePrice;
        private String expiryDate;
        private long callOI;
        private long callChangeInOI;
        private double callLTP;
        private double callIV;
        private long callVolume;
        private long putOI;
        private long putChangeInOI;
        private double putLTP;
        private double putIV;
        private long putVolume;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarketSnapshotData {
        private double niftyValue;
        private double niftyChange;
        private double bankNiftyValue;
        private double bankNiftyChange;
        private double indiaVix;
        private int advances;
        private int declines;
        private double advanceDeclineRatio;
    }
}
