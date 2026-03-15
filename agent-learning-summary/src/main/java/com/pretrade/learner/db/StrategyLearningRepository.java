package com.pretrade.learner.db;

import com.pretrade.shared.models.StrategyLearning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StrategyLearningRepository extends JpaRepository<StrategyLearning, Long> {

    List<StrategyLearning> findByIsActiveTrueOrderByConfidenceDesc();

    List<StrategyLearning> findByLearningDateAndCategory(LocalDate date, String category);

    List<StrategyLearning> findByLearningDateBetweenAndIsActiveTrue(LocalDate start, LocalDate end);
}
