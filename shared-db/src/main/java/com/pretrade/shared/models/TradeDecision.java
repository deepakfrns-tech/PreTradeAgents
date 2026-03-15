package com.pretrade.shared.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "trade_decisions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tradeDate", "signalId"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate tradeDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signalId")
    private StockAnalysis signalId;

    private String symbol;

    private String decision;

    private LocalDateTime decisionTime;

    @Column(columnDefinition = "TEXT")
    private String decisionReason;

    private Double overrideStrike;

    private Double overrideSl;

    private Double overrideTarget;

    private Integer overrideLotSize;

    private String overrideDirection;

    private Double finalStrike;

    private Double finalSl;

    private Double finalTarget;

    private Integer finalLotSize;

    private String finalDirection;
}
