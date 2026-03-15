package com.pretrade.dashboard.service;

import com.pretrade.shared.models.StockAnalysis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CsvParserService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public List<StockAnalysis> parseCsv(MultipartFile file) {
        List<StockAnalysis> analyses = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                log.warn("Empty CSV file uploaded");
                return analyses;
            }

            String line;
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                try {
                    String[] fields = parseCsvLine(line);
                    StockAnalysis sa = mapToStockAnalysis(fields);
                    if (sa != null) {
                        analyses.add(sa);
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse CSV line {}: {}", lineNum, e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("Failed to parse CSV file: {}", e.getMessage(), e);
        }

        log.info("Parsed {} stock analyses from CSV", analyses.size());
        return analyses;
    }

    private StockAnalysis mapToStockAnalysis(String[] fields) {
        if (fields.length < 31) {
            log.warn("CSV row has {} fields, expected 31", fields.length);
            return null;
        }

        return StockAnalysis.builder()
                .tradeDate(parseDate(fields[0]))
                .symbol(fields[1].trim())
                .gapPercent(parseDouble(fields[2]))
                .gapDirection(fields[3].trim())
                .gapCategory(fields[4].trim())
                .gapScore(parseDouble(fields[5]))
                .atrRatio(parseDouble(fields[6]))
                .sentimentScore(parseDouble(fields[7]))
                .sentimentLevel(fields[8].trim())
                .sentimentReasoning(fields[9].trim())
                .volumeRatio(parseDouble(fields[10]))
                .volumeLevel(fields[11].trim())
                .volumeScore(parseDouble(fields[12]))
                .vwapPosition(parseDouble(fields[13]))
                .pcr(parseDouble(fields[14]))
                .oiBuildup(fields[15].trim())
                .maxPain(parseDouble(fields[16]))
                .ivPercentile(parseDouble(fields[17]))
                .oiScore(parseDouble(fields[18]))
                .suggestedStrike(parseDouble(fields[19]))
                .compositeScore(parseDouble(fields[20]))
                .signalDirection(fields[21].trim())
                .recommendedAction(fields[22].trim())
                .confidenceLevel(fields[23].trim())
                .entryStrike(parseDouble(fields[24]))
                .estimatedPremium(parseDouble(fields[25]))
                .stopLoss(parseDouble(fields[26]))
                .target(parseDouble(fields[27]))
                .riskRewardRatio(parseDouble(fields[28]))
                .claudeReasoning(fields[29].trim())
                .riskWarnings(fields[30].trim())
                .build();
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        return LocalDate.parse(value.trim(), DATE_FMT);
    }

    private Double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parses a CSV line handling quoted fields with commas and escaped quotes.
     */
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++; // skip escaped quote
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields.add(current.toString());
                    current = new StringBuilder();
                } else {
                    current.append(c);
                }
            }
        }
        fields.add(current.toString());

        return fields.toArray(new String[0]);
    }
}
