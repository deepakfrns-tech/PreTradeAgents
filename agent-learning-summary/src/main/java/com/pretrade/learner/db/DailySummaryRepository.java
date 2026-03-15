package com.pretrade.learner.db;

import com.pretrade.shared.models.DailySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailySummaryRepository extends JpaRepository<DailySummary, Long> {

    Optional<DailySummary> findByTradeDate(LocalDate tradeDate);

    List<DailySummary> findByTradeDateBetweenOrderByTradeDateDesc(LocalDate start, LocalDate end);
}
