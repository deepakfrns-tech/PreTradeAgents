package com.pretrade.dashboard;

import com.pretrade.dashboard.service.CsvParserService;
import com.pretrade.shared.models.StockAnalysis;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvParserServiceTest {

    private final CsvParserService parser = new CsvParserService();

    @Test
    void parseCsv_validFile_returnsAnalyses() {
        String csv = "trade_date,symbol,gap_percent,gap_direction,gap_category,gap_score,atr_ratio,"
                + "sentiment_score,sentiment_level,sentiment_reasoning,"
                + "volume_ratio,volume_level,volume_score,vwap_position,"
                + "pcr,oi_buildup,max_pain,iv_percentile,oi_score,suggested_strike,"
                + "composite_score,signal_direction,recommended_action,confidence_level,"
                + "entry_strike,estimated_premium,stop_loss,target,risk_reward_ratio,"
                + "claude_reasoning,risk_warnings\n"
                + "2026-03-15,NIFTY,1.5,UP,MODERATE,7.5,1.2,"
                + "8.0,BULLISH,Strong buying sentiment,"
                + "1.8,HIGH,7.0,0.5,"
                + "0.85,LONG_BUILD,22500,45.0,8.5,22600,"
                + "78.5,BULLISH,BUY CE,HIGH,"
                + "22600,150,22400,22900,2.0,"
                + "Gap up with strong OI buildup,VIX elevated\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "trade-signals-2026-03-15.csv",
                "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        List<StockAnalysis> result = parser.parseCsv(file);

        assertEquals(1, result.size());
        StockAnalysis sa = result.get(0);
        assertEquals("NIFTY", sa.getSymbol());
        assertEquals(78.5, sa.getCompositeScore());
        assertEquals("BULLISH", sa.getSignalDirection());
        assertEquals("BUY CE", sa.getRecommendedAction());
    }

    @Test
    void parseCsv_emptyFile_returnsEmpty() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.csv", "text/csv", new byte[0]);

        List<StockAnalysis> result = parser.parseCsv(file);
        assertTrue(result.isEmpty());
    }

    @Test
    void parseCsv_quotedFieldsWithCommas_parsesCorrectly() {
        String csv = "trade_date,symbol,gap_percent,gap_direction,gap_category,gap_score,atr_ratio,"
                + "sentiment_score,sentiment_level,sentiment_reasoning,"
                + "volume_ratio,volume_level,volume_score,vwap_position,"
                + "pcr,oi_buildup,max_pain,iv_percentile,oi_score,suggested_strike,"
                + "composite_score,signal_direction,recommended_action,confidence_level,"
                + "entry_strike,estimated_premium,stop_loss,target,risk_reward_ratio,"
                + "claude_reasoning,risk_warnings\n"
                + "2026-03-15,RELIANCE,2.0,UP,LARGE,8.0,1.5,"
                + "7.5,POSITIVE,\"Strong results, beat estimates\","
                + "2.1,HIGH,8.0,0.8,"
                + "0.9,LONG_BUILD,2800,50.0,8.0,2850,"
                + "82.0,BULLISH,BUY CE,HIGH,"
                + "2850,200,2750,3000,2.5,"
                + "\"Strong gap, good OI support\",\"IV high, premium risk\"\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        List<StockAnalysis> result = parser.parseCsv(file);
        assertEquals(1, result.size());
        assertEquals("Strong results, beat estimates", result.get(0).getSentimentReasoning());
    }
}
