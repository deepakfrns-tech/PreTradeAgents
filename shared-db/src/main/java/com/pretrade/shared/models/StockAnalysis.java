package com.pretrade.shared.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "stock_analysis", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tradeDate", "symbol"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate tradeDate;

    private String symbol;

    private Double gapPercent;

    private String gapDirection;

    private Double atrRatio;

    private String gapCategory;

    private Double gapScore;

    private Double sentimentScore;

    private String sentimentLevel;

    @Column(columnDefinition = "TEXT")
    private String sentimentReasoning;

    @Column(columnDefinition = "jsonb")
    private String headlineDetails;

    private Double volumeRatio;

    private String volumeLevel;

    private Double vwapPosition;

    private Double volumeScore;

    private Double pcr;

    private String oiBuildup;

    private Double maxPain;

    private Double ivPercentile;

    private Double suggestedStrike;

    private Double oiScore;

    private Double compositeScore;

    private String signalDirection;

    private String recommendedAction;

    @Column(columnDefinition = "TEXT")
    private String claudeReasoning;

    @Column(columnDefinition = "TEXT")
    private String riskWarnings;

    private String confidenceLevel;

    private Double entryStrike;

    private Double estimatedPremium;

    private Double stopLoss;

    private Double target;

    private Double riskRewardRatio;
}
