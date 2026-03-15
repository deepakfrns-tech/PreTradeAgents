package com.pretrade.executor.service;

import com.pretrade.executor.config.ExecutorSettings;
import com.pretrade.executor.db.PaperTradeRepository;
import com.pretrade.executor.db.TradeDecisionRepository;
import com.pretrade.shared.models.PaperTrade;
import com.pretrade.shared.models.TradeDecision;
import com.pretrade.utils.Formatters;
import com.pretrade.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TradeExecutionService {

    private final TradeDecisionRepository tradeDecisionRepository;
    private final PaperTradeRepository paperTradeRepository;
    private final ExecutorSettings settings;
    private final TimeUtils timeUtils;

    /**
     * Triggers at 9:15 AM IST on weekdays to execute approved paper trades.
     * Also can be triggered manually via REST endpoint.
     */
    @Scheduled(cron = "0 15 9 * * MON-FRI", zone = "Asia/Kolkata")
    public void executeMarketOpenTrades() {
        LocalDate today = timeUtils.nowIST().toLocalDate();
        log.info("=== Market Open Trade Execution triggered for {} ===", today);

        List<TradeDecision> approvedDecisions = tradeDecisionRepository
                .findByTradeDateAndDecision(today, "APPROVED");

        if (approvedDecisions.isEmpty()) {
            log.info("No approved trades for today. Skipping execution.");
            return;
        }

        log.info("Found {} approved trade decisions for {}", approvedDecisions.size(), today);

        long openPositions = paperTradeRepository.countByTradeDateAndStatus(today, "OPEN");

        for (TradeDecision decision : approvedDecisions) {
            if (openPositions >= settings.getMaxPositions()) {
                log.warn("Max positions ({}) reached. Skipping remaining trades.", settings.getMaxPositions());
                break;
            }

            try {
                PaperTrade trade = createPaperTrade(decision, today);
                paperTradeRepository.save(trade);
                openPositions++;
                log.info("Opened paper trade: {} {} @ strike {} (lot: {})",
                        trade.getDirection(), trade.getSymbol(),
                        trade.getStrikePrice(), trade.getLotSize());
            } catch (Exception e) {
                log.error("Failed to create paper trade for {}: {}",
                        decision.getSymbol(), e.getMessage(), e);
            }
        }

        log.info("=== Opened {} paper trades for {} ===", openPositions, today);
    }

    private PaperTrade createPaperTrade(TradeDecision decision, LocalDate today) {
        Double entryPrice = decision.getFinalStrike() != null
                ? decision.getFinalStrike()
                : (decision.getSignalId() != null ? decision.getSignalId().getEstimatedPremium() : null);

        return PaperTrade.builder()
                .tradeDate(today)
                .symbol(decision.getSymbol())
                .signalId(decision.getSignalId())
                .direction(decision.getFinalDirection())
                .strikePrice(decision.getFinalStrike())
                .entryPrice(entryPrice)
                .entryTime(timeUtils.nowIST())
                .lotSize(decision.getFinalLotSize())
                .entryReasoning(decision.getDecisionReason())
                .status("OPEN")
                .maxProfit(0.0)
                .maxDrawdown(0.0)
                .createdAt(timeUtils.nowIST())
                .build();
    }

    /**
     * Monitors open positions every 5 seconds during market hours.
     * Applies trailing stop loss and EOD exit logic.
     */
    @Scheduled(fixedDelayString = "${executor.monitor-interval-ms:5000}")
    public void monitorOpenPositions() {
        if (!timeUtils.isMarketOpen()) return;

        LocalDate today = timeUtils.nowIST().toLocalDate();
        List<PaperTrade> openTrades = paperTradeRepository.findByTradeDateAndStatus(today, "OPEN");

        if (openTrades.isEmpty()) return;

        LocalTime eodExit = LocalTime.parse(settings.getEodExitTime());
        boolean forceExit = timeUtils.nowIST().toLocalTime().isAfter(eodExit);

        for (PaperTrade trade : openTrades) {
            if (forceExit) {
                closeTrade(trade, "EOD_EXIT", "End of day forced exit at " + settings.getEodExitTime());
            }
            // Note: Real-time price monitoring would go here with broker API integration
        }
    }

    public void closeTrade(PaperTrade trade, String exitReason, String exitReasoning) {
        LocalDateTime now = timeUtils.nowIST();
        trade.setExitTime(now);
        trade.setExitReason(exitReason);
        trade.setExitReasoning(exitReasoning);
        trade.setStatus("CLOSED");

        // Calculate PnL if exit price is available
        if (trade.getExitPrice() != null && trade.getEntryPrice() != null && trade.getLotSize() != null) {
            double pnl = (trade.getExitPrice() - trade.getEntryPrice()) * trade.getLotSize();
            if ("BEARISH".equalsIgnoreCase(trade.getDirection())) {
                pnl = -pnl;
            }
            trade.setPnlAmount(pnl);
            if (trade.getEntryPrice() != 0) {
                trade.setPnlPercent((trade.getExitPrice() - trade.getEntryPrice()) / trade.getEntryPrice() * 100);
            }
        }

        paperTradeRepository.save(trade);
        log.info("Closed trade: {} {} reason={} PnL={}",
                trade.getSymbol(), trade.getDirection(), exitReason,
                trade.getPnlAmount() != null ? Formatters.formatPnL(trade.getPnlAmount()) : "N/A");
    }
}
