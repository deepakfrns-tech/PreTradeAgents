package com.pretrade.shared.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "market_snapshots")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate tradeDate;

    private LocalDateTime timestamp;

    private Double niftyGapPercent;

    private String marketSentiment;

    private Double indiaVix;

    private Double advanceDeclineRatio;

    @Column(columnDefinition = "jsonb")
    private String fiiDiiData;

    @Column(columnDefinition = "jsonb")
    private String rawPreMarketData;
}
