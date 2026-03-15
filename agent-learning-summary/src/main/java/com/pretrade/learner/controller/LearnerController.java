package com.pretrade.learner.controller;

import com.pretrade.learner.db.DailySummaryRepository;
import com.pretrade.learner.db.StrategyLearningRepository;
import com.pretrade.learner.service.LearningSummaryService;
import com.pretrade.shared.models.DailySummary;
import com.pretrade.shared.models.StrategyLearning;
import com.pretrade.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/learner")
@Slf4j
@RequiredArgsConstructor
public class LearnerController {

    private final LearningSummaryService learningSummaryService;
    private final DailySummaryRepository dailySummaryRepository;
    private final StrategyLearningRepository strategyLearningRepository;
    private final TimeUtils timeUtils;

    @PostMapping("/generate-summary")
    public ResponseEntity<DailySummary> generateSummary(
            @RequestParam(required = false) String date) {
        LocalDate tradeDate = (date != null)
                ? LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
                : timeUtils.nowIST().toLocalDate();

        DailySummary summary = learningSummaryService.generateDailySummary(tradeDate);
        if (summary == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/mine-patterns")
    public ResponseEntity<List<StrategyLearning>> minePatterns(
            @RequestParam(required = false) String date) {
        LocalDate tradeDate = (date != null)
                ? LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
                : timeUtils.nowIST().toLocalDate();

        List<StrategyLearning> learnings = learningSummaryService.minePatterns(tradeDate);
        return ResponseEntity.ok(learnings);
    }

    @GetMapping("/summary/{date}")
    public ResponseEntity<DailySummary> getSummary(@PathVariable String date) {
        LocalDate tradeDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        Optional<DailySummary> summary = dailySummaryRepository.findByTradeDate(tradeDate);
        return summary.map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/learnings")
    public ResponseEntity<List<StrategyLearning>> getActiveLearnings() {
        List<StrategyLearning> learnings = strategyLearningRepository
                .findByIsActiveTrueOrderByConfidenceDesc();
        return ResponseEntity.ok(learnings);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "agent", "learning-summary",
                "time", timeUtils.nowIST().toString()
        ));
    }
}
