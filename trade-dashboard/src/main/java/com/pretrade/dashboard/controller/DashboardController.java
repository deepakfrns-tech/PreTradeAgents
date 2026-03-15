package com.pretrade.dashboard.controller;

import com.pretrade.dashboard.db.StockAnalysisRepository;
import com.pretrade.dashboard.db.TradeDecisionRepository;
import com.pretrade.dashboard.service.CsvParserService;
import com.pretrade.shared.models.StockAnalysis;
import com.pretrade.shared.models.TradeDecision;
import com.pretrade.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Controller
@Slf4j
@RequiredArgsConstructor
public class DashboardController {

    private final StockAnalysisRepository stockAnalysisRepository;
    private final TradeDecisionRepository tradeDecisionRepository;
    private final CsvParserService csvParserService;
    private final TimeUtils timeUtils;

    @GetMapping("/")
    public String index(Model model) {
        LocalDate today = timeUtils.nowIST().toLocalDate();
        return showDashboard(today.format(DateTimeFormatter.ISO_LOCAL_DATE), model);
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) String date, Model model) {
        if (date == null || date.isEmpty()) {
            date = timeUtils.nowIST().toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        return showDashboard(date, model);
    }

    private String showDashboard(String date, Model model) {
        LocalDate tradeDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        List<StockAnalysis> signals = stockAnalysisRepository
                .findByTradeDateOrderByCompositeScoreDesc(tradeDate);
        List<TradeDecision> decisions = tradeDecisionRepository
                .findByTradeDateOrderByDecisionTimeDesc(tradeDate);

        List<Long> approvedSignalIds = decisions.stream()
                .filter(d -> "APPROVED".equals(d.getDecision()))
                .map(d -> d.getSignalId().getId())
                .toList();

        model.addAttribute("signals", signals);
        model.addAttribute("decisions", decisions);
        model.addAttribute("approvedSignalIds", approvedSignalIds);
        model.addAttribute("tradeDate", date);
        model.addAttribute("currentTime", timeUtils.nowIST().toString());
        return "dashboard";
    }

    @GetMapping("/upload")
    public String uploadPage() {
        return "upload";
    }

    @PostMapping("/upload")
    public String handleUpload(@RequestParam("file") MultipartFile file,
                                RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a CSV file to upload.");
            return "redirect:/upload";
        }

        List<StockAnalysis> analyses = csvParserService.parseCsv(file);

        if (analyses.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No valid signals found in the CSV file.");
            return "redirect:/upload";
        }

        int saved = 0;
        int updated = 0;
        for (StockAnalysis sa : analyses) {
            Optional<StockAnalysis> existing = stockAnalysisRepository
                    .findByTradeDateAndSymbol(sa.getTradeDate(), sa.getSymbol());
            if (existing.isPresent()) {
                StockAnalysis ex = existing.get();
                copyFields(sa, ex);
                stockAnalysisRepository.save(ex);
                updated++;
            } else {
                stockAnalysisRepository.save(sa);
                saved++;
            }
        }

        String date = analyses.get(0).getTradeDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
        redirectAttributes.addFlashAttribute("success",
                String.format("Imported %d new signals, updated %d existing signals.", saved, updated));
        return "redirect:/dashboard?date=" + date;
    }

    @PostMapping("/approve-trades")
    public String approveTrades(@RequestParam("signalIds") List<Long> signalIds,
                                 @RequestParam("tradeDate") String date,
                                 RedirectAttributes redirectAttributes) {
        LocalDate tradeDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);

        int approved = 0;
        for (Long signalId : signalIds) {
            Optional<StockAnalysis> signalOpt = stockAnalysisRepository.findById(signalId);
            if (signalOpt.isEmpty()) continue;

            StockAnalysis signal = signalOpt.get();

            // Check if already approved
            List<TradeDecision> existingDecisions = tradeDecisionRepository
                    .findByTradeDateAndDecision(tradeDate, "APPROVED");
            boolean alreadyApproved = existingDecisions.stream()
                    .anyMatch(d -> d.getSignalId().getId().equals(signalId));
            if (alreadyApproved) continue;

            TradeDecision decision = TradeDecision.builder()
                    .tradeDate(tradeDate)
                    .signalId(signal)
                    .symbol(signal.getSymbol())
                    .decision("APPROVED")
                    .decisionTime(timeUtils.nowIST())
                    .decisionReason("Approved via dashboard")
                    .finalStrike(signal.getEntryStrike())
                    .finalSl(signal.getStopLoss())
                    .finalTarget(signal.getTarget())
                    .finalLotSize(com.pretrade.utils.LotSizes.getLotSize(signal.getSymbol()))
                    .finalDirection(signal.getSignalDirection())
                    .build();

            tradeDecisionRepository.save(decision);
            approved++;
        }

        redirectAttributes.addFlashAttribute("success",
                String.format("Approved %d trade(s) for execution.", approved));
        return "redirect:/dashboard?date=" + date;
    }

    private void copyFields(StockAnalysis source, StockAnalysis target) {
        target.setGapPercent(source.getGapPercent());
        target.setGapDirection(source.getGapDirection());
        target.setGapCategory(source.getGapCategory());
        target.setGapScore(source.getGapScore());
        target.setAtrRatio(source.getAtrRatio());
        target.setSentimentScore(source.getSentimentScore());
        target.setSentimentLevel(source.getSentimentLevel());
        target.setSentimentReasoning(source.getSentimentReasoning());
        target.setHeadlineDetails(source.getHeadlineDetails());
        target.setVolumeRatio(source.getVolumeRatio());
        target.setVolumeLevel(source.getVolumeLevel());
        target.setVolumeScore(source.getVolumeScore());
        target.setVwapPosition(source.getVwapPosition());
        target.setPcr(source.getPcr());
        target.setOiBuildup(source.getOiBuildup());
        target.setMaxPain(source.getMaxPain());
        target.setIvPercentile(source.getIvPercentile());
        target.setOiScore(source.getOiScore());
        target.setSuggestedStrike(source.getSuggestedStrike());
        target.setCompositeScore(source.getCompositeScore());
        target.setSignalDirection(source.getSignalDirection());
        target.setRecommendedAction(source.getRecommendedAction());
        target.setConfidenceLevel(source.getConfidenceLevel());
        target.setEntryStrike(source.getEntryStrike());
        target.setEstimatedPremium(source.getEstimatedPremium());
        target.setStopLoss(source.getStopLoss());
        target.setTarget(source.getTarget());
        target.setRiskRewardRatio(source.getRiskRewardRatio());
        target.setClaudeReasoning(source.getClaudeReasoning());
        target.setRiskWarnings(source.getRiskWarnings());
    }
}
