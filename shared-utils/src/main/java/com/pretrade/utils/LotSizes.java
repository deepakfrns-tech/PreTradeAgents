package com.pretrade.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Registry of NSE F&O (Futures and Options) lot sizes for major stocks and indices.
 * Provides lookup methods for lot sizes and validation of F&O-eligible symbols.
 */
@Slf4j
@Component
public class LotSizes {

    private static final Map<String, Integer> LOT_SIZE_MAP;

    static {
        Map<String, Integer> map = new LinkedHashMap<>();

        // Indices
        map.put("NIFTY", 50);
        map.put("BANKNIFTY", 15);
        map.put("FINNIFTY", 40);
        map.put("MIDCPNIFTY", 75);

        // Large-cap stocks
        map.put("RELIANCE", 250);
        map.put("TCS", 175);
        map.put("INFY", 300);
        map.put("HDFCBANK", 550);
        map.put("ICICIBANK", 1375);
        map.put("BAJFINANCE", 125);
        map.put("SBIN", 1500);
        map.put("ITC", 1600);
        map.put("TATAMOTORS", 1400);
        map.put("HINDUNILVR", 300);
        map.put("KOTAKBANK", 400);
        map.put("LT", 375);
        map.put("AXISBANK", 625);
        map.put("BHARTIARTL", 475);
        map.put("ASIANPAINT", 300);
        map.put("MARUTI", 100);
        map.put("HCLTECH", 350);
        map.put("SUNPHARMA", 700);
        map.put("TITAN", 375);
        map.put("ULTRACEMCO", 100);
        map.put("WIPRO", 1500);
        map.put("ADANIENT", 500);
        map.put("ADANIPORTS", 1250);
        map.put("BAJAJ-AUTO", 250);
        map.put("BAJAJFINSV", 500);
        map.put("BPCL", 1800);
        map.put("BRITANNIA", 200);
        map.put("CIPLA", 650);
        map.put("COALINDIA", 2100);
        map.put("DIVISLAB", 200);
        map.put("DRREDDY", 125);
        map.put("EICHERMOT", 175);
        map.put("GRASIM", 350);
        map.put("HDFCLIFE", 1100);
        map.put("HEROMOTOCO", 300);
        map.put("HINDALCO", 1400);
        map.put("INDUSINDBK", 500);
        map.put("JSWSTEEL", 1350);
        map.put("M&M", 700);
        map.put("NESTLEIND", 50);
        map.put("NTPC", 2800);
        map.put("ONGC", 3850);
        map.put("POWERGRID", 2700);
        map.put("SBILIFE", 750);
        map.put("TATACONSUM", 675);
        map.put("TATASTEEL", 5500);
        map.put("TECHM", 600);
        map.put("APOLLOHOSP", 250);
        map.put("DMART", 200);
        map.put("LTIM", 150);
        map.put("PIDILITIND", 500);
        map.put("SIEMENS", 275);
        map.put("HAL", 300);
        map.put("IOC", 4300);
        map.put("PNB", 6000);
        map.put("BANKBARODA", 2925);
        map.put("ZOMATO", 6250);
        map.put("TATAPOWER", 2700);
        map.put("DLF", 1375);
        map.put("BEL", 3700);

        LOT_SIZE_MAP = Collections.unmodifiableMap(map);
    }

    /**
     * Returns the F&O lot size for the given symbol.
     *
     * @param symbol the stock/index symbol (case-insensitive)
     * @return the lot size, or -1 if the symbol is not a valid F&O stock
     */
    public int getLotSize(String symbol) {
        if (symbol == null) {
            return -1;
        }
        Integer lotSize = LOT_SIZE_MAP.get(symbol.toUpperCase().trim());
        if (lotSize == null) {
            log.warn("Lot size not found for symbol: {}", symbol);
            return -1;
        }
        return lotSize;
    }

    /**
     * Checks whether a given symbol is a valid F&O stock/index.
     *
     * @param symbol the stock/index symbol (case-insensitive)
     * @return true if the symbol exists in the F&O lot size registry
     */
    public boolean isValidFnOStock(String symbol) {
        if (symbol == null) {
            return false;
        }
        return LOT_SIZE_MAP.containsKey(symbol.toUpperCase().trim());
    }

    /**
     * Returns all symbols registered in the F&O lot size registry.
     *
     * @return an unmodifiable set of all F&O symbol names
     */
    public Set<String> getAllSymbols() {
        return LOT_SIZE_MAP.keySet();
    }

    /**
     * Returns the entire lot size map (read-only).
     *
     * @return an unmodifiable map of symbol to lot size
     */
    public Map<String, Integer> getAllLotSizes() {
        return LOT_SIZE_MAP;
    }
}
