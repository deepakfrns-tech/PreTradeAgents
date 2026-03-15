package com.pretrade.executor.controller;

import com.pretrade.executor.db.PaperTradeRepository;
import com.pretrade.executor.service.TradeExecutionService;
import com.pretrade.shared.models.PaperTrade;
import com.pretrade.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/executor")
@Slf4j
@RequiredArgsConstructor
public class ExecutorController {

    private final TradeExecutionService tradeExecutionService;
    private final PaperTradeRepository paperTradeRepository;
    private final TimeUtils timeUtils;

    @PostMapping("/trigger")
    public ResponseEntity<Map<String, String>> triggerExecution() {
        log.info("Manual trade execution trigger received");
        tradeExecutionService.executeMarketOpenTrades();
        return ResponseEntity.ok(Map.of("status", "triggered", "time", timeUtils.nowIST().toString()));
    }

    @GetMapping("/trades/{date}")
    public ResponseEntity<List<PaperTrade>> getTrades(@PathVariable String date) {
        LocalDate tradeDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        List<PaperTrade> trades = paperTradeRepository.findByTradeDate(tradeDate);
        return ResponseEntity.ok(trades);
    }

    @GetMapping("/trades/{date}/open")
    public ResponseEntity<List<PaperTrade>> getOpenTrades(@PathVariable String date) {
        LocalDate tradeDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        List<PaperTrade> trades = paperTradeRepository.findByTradeDateAndStatus(tradeDate, "OPEN");
        return ResponseEntity.ok(trades);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "agent", "trade-executor",
                "time", timeUtils.nowIST().toString()
        ));
    }
}
