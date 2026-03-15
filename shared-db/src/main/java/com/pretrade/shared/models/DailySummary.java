package com.pretrade.shared.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_summaries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private LocalDate tradeDate;

    private Integer totalTrades;

    private Integer winningTrades;

    private Integer losingTrades;

    private Double winRate;

    private Double totalPnl;

    private Double profitFactor;

    private Double maxDrawdown;

    private Double sharpeRatio;

    private String marketRegime;

    @Column(columnDefinition = "TEXT")
    private String dailyNarrative;

    @Column(columnDefinition = "jsonb")
    private String tradePostmortems;

    private Integer signalsShown;

    private Integer signalsApproved;

    private Integer signalsSkipped;

    private Double approvalAccuracy;

    private Double skipAccuracy;

    @Column(columnDefinition = "jsonb")
    private String overrideImpact;

    @Column(columnDefinition = "TEXT")
    private String decisionNarrative;

    @Column(columnDefinition = "jsonb")
    private String missedOpportunities;

    @Column(columnDefinition = "jsonb")
    private String patternsIdentified;

    @Column(columnDefinition = "jsonb")
    private String recommendedAdjustments;

    private LocalDateTime createdAt;
}
