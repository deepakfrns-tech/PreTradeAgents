package com.pretrade.shared.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "strategy_learnings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StrategyLearning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate learningDate;

    private String category;

    @Column(columnDefinition = "TEXT")
    private String insight;

    @Column(columnDefinition = "jsonb")
    private String evidence;

    private Double confidence;

    @Builder.Default
    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer timesValidated = 0;

    @Builder.Default
    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isActive = true;

    private LocalDateTime createdAt;
}
