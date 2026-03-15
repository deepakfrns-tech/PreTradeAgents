package com.pretrade.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LotSizesTest {

    private LotSizes lotSizes;

    @BeforeEach
    void setUp() {
        lotSizes = new LotSizes();
    }

    // --- getLotSize tests ---

    @Test
    void getLotSize_nifty() {
        assertEquals(50, lotSizes.getLotSize("NIFTY"));
    }

    @Test
    void getLotSize_bankNifty() {
        assertEquals(15, lotSizes.getLotSize("BANKNIFTY"));
    }

    @Test
    void getLotSize_reliance() {
        assertEquals(250, lotSizes.getLotSize("RELIANCE"));
    }

    @Test
    void getLotSize_tcs() {
        assertEquals(175, lotSizes.getLotSize("TCS"));
    }

    @Test
    void getLotSize_caseInsensitive() {
        assertEquals(50, lotSizes.getLotSize("nifty"));
        assertEquals(50, lotSizes.getLotSize("Nifty"));
    }

    @Test
    void getLotSize_withWhitespace() {
        assertEquals(50, lotSizes.getLotSize("  NIFTY  "));
    }

    @Test
    void getLotSize_invalidSymbol() {
        assertEquals(-1, lotSizes.getLotSize("INVALIDXYZ"));
    }

    @Test
    void getLotSize_null() {
        assertEquals(-1, lotSizes.getLotSize(null));
    }

    // --- isValidFnOStock tests ---

    @Test
    void isValidFnOStock_validSymbol() {
        assertTrue(lotSizes.isValidFnOStock("NIFTY"));
        assertTrue(lotSizes.isValidFnOStock("RELIANCE"));
        assertTrue(lotSizes.isValidFnOStock("INFY"));
    }

    @Test
    void isValidFnOStock_invalidSymbol() {
        assertFalse(lotSizes.isValidFnOStock("INVALIDXYZ"));
    }

    @Test
    void isValidFnOStock_null() {
        assertFalse(lotSizes.isValidFnOStock(null));
    }

    @Test
    void isValidFnOStock_caseInsensitive() {
        assertTrue(lotSizes.isValidFnOStock("reliance"));
    }

    // --- getAllSymbols tests ---

    @Test
    void getAllSymbols_containsIndices() {
        Set<String> symbols = lotSizes.getAllSymbols();
        assertTrue(symbols.contains("NIFTY"));
        assertTrue(symbols.contains("BANKNIFTY"));
        assertTrue(symbols.contains("FINNIFTY"));
        assertTrue(symbols.contains("MIDCPNIFTY"));
    }

    @Test
    void getAllSymbols_containsMajorStocks() {
        Set<String> symbols = lotSizes.getAllSymbols();
        assertTrue(symbols.contains("RELIANCE"));
        assertTrue(symbols.contains("TCS"));
        assertTrue(symbols.contains("INFY"));
        assertTrue(symbols.contains("HDFCBANK"));
    }

    @Test
    void getAllSymbols_isNotEmpty() {
        assertFalse(lotSizes.getAllSymbols().isEmpty());
        assertTrue(lotSizes.getAllSymbols().size() >= 50);
    }

    // --- getAllLotSizes tests ---

    @Test
    void getAllLotSizes_returnsUnmodifiableMap() {
        Map<String, Integer> map = lotSizes.getAllLotSizes();
        assertThrows(UnsupportedOperationException.class, () -> map.put("TEST", 100));
    }

    @Test
    void getAllLotSizes_allValuesPositive() {
        for (Map.Entry<String, Integer> entry : lotSizes.getAllLotSizes().entrySet()) {
            assertTrue(entry.getValue() > 0,
                    "Lot size for " + entry.getKey() + " should be positive");
        }
    }
}
