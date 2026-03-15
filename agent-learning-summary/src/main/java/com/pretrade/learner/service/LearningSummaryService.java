package com.pretrade.learner.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pretrade.learner.config.LearnerSettings;
import com.pretrade.learner.db.*;
import com.pretrade.shared.models.*;
import com.pretrade.utils.Formatters;
import com.pretrade.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class LearningSummaryService {

    private final PaperTradeRepository paperTradeRepository;
    private final DailySummaryRepository dailySummaryRepository;
    private final StrategyLearningRepository strategyLearningRepository;
    private final StockAnalysisRepository stockAnalysisRepository;
    private final TradeDecisionRepository tradeDecisionRepository;
    private final LearnerSettings settings;
    private final TimeUtils timeUtils;
    private final ObjectMapper objectMapper;

    /**
     * Generates the daily summary and strategy learnings for a given date.
     * Typically run post-market (~4:00 PM IST).
     */
    public DailySummary generateDailySummary(LocalDate tradeDate) {
        log.info("=== Generating daily summary for {} ===", tradeDate);

        List<PaperTrade> trades = paperTradeRepository.findByTradeDate(tradeDate);
        List<StockAnalysis> signals = stockAnalysisRepository.findByTradeDate(tradeDate);
        List<TradeDecision> decisions = tradeDecisionRepository.findByTradeDate(tradeDate);

        if (trades.isEmpty() && signals.isEmpty()) {
            log.info("No trades or signals for {}. Skipping summary.", tradeDate);
            return null;
        }

        // Calculate trade metrics
        List<PaperTrade> closedTrades = trades.stream()
                .filter(t -> "CLOSED".equals(t.getStatus()))
                .toList();

        int totalTrades = closedTrades.size();
        int winningTrades = (int) closedTrades.stream()
                .filter(t -> t.getPnlAmount() != null && t.getPnlAmount() > 0)
                .count();
        int losingTrades = (int) closedTrades.stream()
                .filter(t -> t.getPnlAmount() != null && t.getPnlAmount() < 0)
                .count();

        double totalPnl = closedTrades.stream()
                .filter(t -> t.getPnlAmount() != null)
                .mapToDouble(PaperTrade::getPnlAmount)
                .sum();

        double winRate = totalTrades > 0 ? (double) winningTrades / totalTrades * 100 : 0;

        double totalProfit = closedTrades.stream()
                .filter(t -> t.getPnlAmount() != null && t.getPnlAmount() > 0)
                .mapToDouble(PaperTrade::getPnlAmount)
                .sum();
        double totalLoss = Math.abs(closedTrades.stream()
                .filter(t -> t.getPnlAmount() != null && t.getPnlAmount() < 0)
                .mapToDouble(PaperTrade::getPnlAmount)
                .sum());
        double profitFactor = totalLoss > 0 ? totalProfit / totalLoss : (totalProfit > 0 ? Double.MAX_VALUE : 0);

        double maxDrawdown = closedTrades.stream()
                .filter(t -> t.getMaxDrawdown() != null)
                .mapToDouble(PaperTrade::getMaxDrawdown)
                .max()
                .orElse(0);

        // Decision accuracy
        int signalsShown = signals.size();
        int signalsApproved = (int) decisions.stream()
                .filter(d -> "APPROVED".equals(d.getDecision()))
                .count();
        int signalsSkipped = signalsShown - signalsApproved;

        // Build trade postmortems
        String tradePostmortems = buildTradePostmortems(closedTrades);

        // Build narrative
        String dailyNarrative = buildDailyNarrative(totalTrades, winningTrades, losingTrades,
                totalPnl, winRate, profitFactor);

        // Create or update summary
        Optional<DailySummary> existing = dailySummaryRepository.findByTradeDate(tradeDate);
        DailySummary summary;
        if (existing.isPresent()) {
            summary = existing.get();
        } else {
            summary = new DailySummary();
            summary.setTradeDate(tradeDate);
        }

        summary.setTotalTrades(totalTrades);
        summary.setWinningTrades(winningTrades);
        summary.setLosingTrades(losingTrades);
        summary.setWinRate(winRate);
        summary.setTotalPnl(totalPnl);
        summary.setProfitFactor(profitFactor);
        summary.setMaxDrawdown(maxDrawdown);
        summary.setSignalsShown(signalsShown);
        summary.setSignalsApproved(signalsApproved);
        summary.setSignalsSkipped(signalsSkipped);
        summary.setDailyNarrative(dailyNarrative);
        summary.setTradePostmortems(tradePostmortems);
        summary.setCreatedAt(timeUtils.nowIST());

        summary = dailySummaryRepository.save(summary);
        log.info("Saved daily summary for {}: {} trades, PnL: {}, Win Rate: {}%",
                tradeDate, totalTrades, Formatters.formatPnL(totalPnl), String.format("%.1f", winRate));

        return summary;
    }

    /**
     * Mines patterns from recent trade history and produces strategy learnings.
     */
    public List<StrategyLearning> minePatterns(LocalDate tradeDate) {
        log.info("=== Mining patterns for {} ===", tradeDate);

        LocalDate lookbackStart = tradeDate.minusDays(settings.getLookbackDays());
        List<PaperTrade> recentTrades = paperTradeRepository
                .findByTradeDateBetween(lookbackStart, tradeDate);

        if (recentTrades.size() < settings.getPatternMinOccurrences()) {
            log.info("Not enough trades ({}) for pattern mining (min: {}).",
                    recentTrades.size(), settings.getPatternMinOccurrences());
            return Collections.emptyList();
        }

        List<StrategyLearning> learnings = new ArrayList<>();

        // Pattern: Win rate by direction
        learnings.addAll(analyzeDirectionPatterns(recentTrades, tradeDate));

        // Pattern: Win rate by gap category
        learnings.addAll(analyzeGapPatterns(recentTrades, tradeDate));

        // Pattern: Time-of-day effectiveness
        learnings.addAll(analyzeTimingPatterns(recentTrades, tradeDate));

        // Save learnings that meet confidence threshold
        List<StrategyLearning> savedLearnings = new ArrayList<>();
        for (StrategyLearning learning : learnings) {
            if (learning.getConfidence() >= settings.getMinConfidenceForLearning()) {
                strategyLearningRepository.save(learning);
                savedLearnings.add(learning);
                log.info("Saved learning: [{}] {} (confidence: {:.2f})",
                        learning.getCategory(), learning.getInsight(), learning.getConfidence());
            }
        }

        log.info("=== Mined {} patterns, saved {} learnings ===",
                learnings.size(), savedLearnings.size());
        return savedLearnings;
    }

    private List<StrategyLearning> analyzeDirectionPatterns(List<PaperTrade> trades, LocalDate date) {
        List<StrategyLearning> learnings = new ArrayList<>();

        Map<String, List<PaperTrade>> byDirection = trades.stream()
                .filter(t -> t.getDirection() != null && "CLOSED".equals(t.getStatus()))
                .collect(Collectors.groupingBy(PaperTrade::getDirection));

        for (Map.Entry<String, List<PaperTrade>> entry : byDirection.entrySet()) {
            String direction = entry.getKey();
            List<PaperTrade> dirTrades = entry.getValue();
            if (dirTrades.size() < settings.getPatternMinOccurrences()) continue;

            long wins = dirTrades.stream()
                    .filter(t -> t.getPnlAmount() != null && t.getPnlAmount() > 0)
                    .count();
            double winRate = (double) wins / dirTrades.size();

            learnings.add(StrategyLearning.builder()
                    .learningDate(date)
                    .category("DIRECTION_BIAS")
                    .insight(String.format("%s trades have %.0f%% win rate over last %d days (%d trades)",
                            direction, winRate * 100, settings.getLookbackDays(), dirTrades.size()))
                    .confidence(calculateConfidence(dirTrades.size(), winRate))
                    .evidence(buildEvidence("direction", direction, "trades", dirTrades.size(), "winRate", winRate))
                    .timesValidated(0)
                    .isActive(true)
                    .createdAt(timeUtils.nowIST())
                    .build());
        }

        return learnings;
    }

    private List<StrategyLearning> analyzeGapPatterns(List<PaperTrade> trades, LocalDate date) {
        // This would cross-reference with StockAnalysis gap data
        // Simplified version for now
        return Collections.emptyList();
    }

    private List<StrategyLearning> analyzeTimingPatterns(List<PaperTrade> trades, LocalDate date) {
        List<StrategyLearning> learnings = new ArrayList<>();

        List<PaperTrade> closedTrades = trades.stream()
                .filter(t -> "CLOSED".equals(t.getStatus()) && t.getEntryTime() != null)
                .toList();

        if (closedTrades.size() < settings.getPatternMinOccurrences()) return learnings;

        // Analyze early entries (within first 15 min) vs later entries
        long earlyEntries = closedTrades.stream()
                .filter(t -> t.getEntryTime().toLocalTime().isBefore(java.time.LocalTime.of(9, 30)))
                .count();

        long earlyWins = closedTrades.stream()
                .filter(t -> t.getEntryTime().toLocalTime().isBefore(java.time.LocalTime.of(9, 30)))
                .filter(t -> t.getPnlAmount() != null && t.getPnlAmount() > 0)
                .count();

        if (earlyEntries >= settings.getPatternMinOccurrences()) {
            double earlyWinRate = (double) earlyWins / earlyEntries;
            learnings.add(StrategyLearning.builder()
                    .learningDate(date)
                    .category("TIMING")
                    .insight(String.format("Early entries (before 9:30) have %.0f%% win rate (%d trades)",
                            earlyWinRate * 100, earlyEntries))
                    .confidence(calculateConfidence((int) earlyEntries, earlyWinRate))
                    .evidence(buildEvidence("timing", "early", "trades", earlyEntries, "winRate", earlyWinRate))
                    .timesValidated(0)
                    .isActive(true)
                    .createdAt(timeUtils.nowIST())
                    .build());
        }

        return learnings;
    }

    private double calculateConfidence(int sampleSize, double winRate) {
        // Confidence increases with sample size and extreme win rates
        double sizeFactor = Math.min(1.0, sampleSize / 20.0);
        double rateFactor = Math.abs(winRate - 0.5) * 2; // distance from 50%
        return Math.min(1.0, sizeFactor * 0.6 + rateFactor * 0.4);
    }

    private String buildEvidence(Object... kvPairs) {
        Map<String, Object> evidence = new LinkedHashMap<>();
        for (int i = 0; i < kvPairs.length; i += 2) {
            evidence.put(kvPairs[i].toString(), kvPairs[i + 1]);
        }
        try {
            return objectMapper.writeValueAsString(evidence);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private String buildTradePostmortems(List<PaperTrade> closedTrades) {
        List<Map<String, Object>> postmortems = new ArrayList<>();
        for (PaperTrade trade : closedTrades) {
            Map<String, Object> pm = new LinkedHashMap<>();
            pm.put("symbol", trade.getSymbol());
            pm.put("direction", trade.getDirection());
            pm.put("entryPrice", trade.getEntryPrice());
            pm.put("exitPrice", trade.getExitPrice());
            pm.put("pnl", trade.getPnlAmount());
            pm.put("exitReason", trade.getExitReason());
            postmortems.add(pm);
        }
        try {
            return objectMapper.writeValueAsString(postmortems);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private String buildDailyNarrative(int total, int wins, int losses, double pnl, double winRate, double pf) {
        return String.format(
                "Day summary: %d trades executed, %d wins, %d losses. "
                        + "Win rate: %.1f%%. Total PnL: %s. Profit factor: %.2f.",
                total, wins, losses, winRate, Formatters.formatPnL(pnl), pf);
    }
}
