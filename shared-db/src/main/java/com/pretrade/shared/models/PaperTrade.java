package com.pretrade.shared.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "paper_trades")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaperTrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate tradeDate;

    private String symbol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signalId")
    private StockAnalysis signalId;

    private String direction;

    private Double strikePrice;

    private Double entryPrice;

    private LocalDateTime entryTime;

    private Integer lotSize;

    @Column(columnDefinition = "TEXT")
    private String entryReasoning;

    private Double exitPrice;

    private LocalDateTime exitTime;

    private String exitReason;

    @Column(columnDefinition = "TEXT")
    private String exitReasoning;

    private Double pnlAmount;

    private Double pnlPercent;

    private Double maxProfit;

    private Double maxDrawdown;

    @Column(columnDefinition = "jsonb")
    private String priceTrail;

    @Column(columnDefinition = "jsonb")
    private String slAdjustments;

    private String status;

    private LocalDateTime createdAt;
}
