package com.pretrade.analyst.service;

import com.pretrade.shared.models.StockAnalysis;
import com.pretrade.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CsvExportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String[] CSV_HEADERS = {
            "trade_date", "symbol", "gap_percent", "gap_direction", "gap_category", "gap_score", "atr_ratio",
            "sentiment_score", "sentiment_level", "sentiment_reasoning",
            "volume_ratio", "volume_level", "volume_score", "vwap_position",
            "pcr", "oi_buildup", "max_pain", "iv_percentile", "oi_score", "suggested_strike",
            "composite_score", "signal_direction", "recommended_action", "confidence_level",
            "entry_strike", "estimated_premium", "stop_loss", "target", "risk_reward_ratio",
            "claude_reasoning", "risk_warnings"
    };

    private final TimeUtils timeUtils;

    @Value("${analyst.csv-output-dir:./output}")
    private String csvOutputDir;

    public Path exportToCsv(List<StockAnalysis> analyses) throws IOException {
        LocalDate tradeDate = analyses.isEmpty()
                ? timeUtils.nowIST().toLocalDate()
                : analyses.get(0).getTradeDate();

        String filename = "trade-signals-" + tradeDate.format(DATE_FMT) + ".csv";
        Path outputDir = Paths.get(csvOutputDir);
        Files.createDirectories(outputDir);
        Path filePath = outputDir.resolve(filename);

        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writer.write(String.join(",", CSV_HEADERS));
            writer.newLine();

            for (StockAnalysis sa : analyses) {
                writer.write(toCsvRow(sa));
                writer.newLine();
            }
        }

        log.info("Exported {} signals to {}", analyses.size(), filePath.toAbsolutePath());
        return filePath;
    }

    private String toCsvRow(StockAnalysis sa) {
        return String.join(",",
                escapeCsv(sa.getTradeDate()),
                escapeCsv(sa.getSymbol()),
                escapeCsv(sa.getGapPercent()),
                escapeCsv(sa.getGapDirection()),
                escapeCsv(sa.getGapCategory()),
                escapeCsv(sa.getGapScore()),
                escapeCsv(sa.getAtrRatio()),
                escapeCsv(sa.getSentimentScore()),
                escapeCsv(sa.getSentimentLevel()),
                escapeCsv(sa.getSentimentReasoning()),
                escapeCsv(sa.getVolumeRatio()),
                escapeCsv(sa.getVolumeLevel()),
                escapeCsv(sa.getVolumeScore()),
                escapeCsv(sa.getVwapPosition()),
                escapeCsv(sa.getPcr()),
                escapeCsv(sa.getOiBuildup()),
                escapeCsv(sa.getMaxPain()),
                escapeCsv(sa.getIvPercentile()),
                escapeCsv(sa.getOiScore()),
                escapeCsv(sa.getSuggestedStrike()),
                escapeCsv(sa.getCompositeScore()),
                escapeCsv(sa.getSignalDirection()),
                escapeCsv(sa.getRecommendedAction()),
                escapeCsv(sa.getConfidenceLevel()),
                escapeCsv(sa.getEntryStrike()),
                escapeCsv(sa.getEstimatedPremium()),
                escapeCsv(sa.getStopLoss()),
                escapeCsv(sa.getTarget()),
                escapeCsv(sa.getRiskRewardRatio()),
                escapeCsv(sa.getClaudeReasoning()),
                escapeCsv(sa.getRiskWarnings())
        );
    }

    private String escapeCsv(Object value) {
        if (value == null) return "";
        String str = value.toString();
        if (str.contains(",") || str.contains("\"") || str.contains("\n") || str.contains("\r")) {
            return "\"" + str.replace("\"", "\"\"") + "\"";
        }
        return str;
    }
}
