package com.pretrade.analyst.controller;

import com.pretrade.analyst.db.StockAnalysisRepository;
import com.pretrade.analyst.service.CsvExportService;
import com.pretrade.shared.models.StockAnalysis;
import com.pretrade.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analyst")
@Slf4j
@RequiredArgsConstructor
public class AnalystController {

    private final StockAnalysisRepository stockAnalysisRepository;
    private final CsvExportService csvExportService;
    private final TimeUtils timeUtils;

    @GetMapping("/signals/{date}")
    public ResponseEntity<List<StockAnalysis>> getSignals(@PathVariable String date) {
        LocalDate tradeDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        List<StockAnalysis> signals = stockAnalysisRepository
                .findByTradeDateOrderByCompositeScoreDesc(tradeDate);
        return ResponseEntity.ok(signals);
    }

    @PostMapping("/export-csv")
    public ResponseEntity<Resource> exportCsv(
            @RequestParam(required = false) String date) {
        try {
            LocalDate tradeDate = (date != null)
                    ? LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
                    : timeUtils.nowIST().toLocalDate();

            List<StockAnalysis> signals = stockAnalysisRepository
                    .findByTradeDateOrderByCompositeScoreDesc(tradeDate);

            if (signals.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            Path csvPath = csvExportService.exportToCsv(signals);
            Resource resource = new FileSystemResource(csvPath);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + csvPath.getFileName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("Failed to export CSV: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "agent", "market-analyst",
                "time", timeUtils.nowIST().toString()
        ));
    }
}
