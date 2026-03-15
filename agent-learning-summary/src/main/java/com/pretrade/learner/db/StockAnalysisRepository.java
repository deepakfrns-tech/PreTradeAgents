package com.pretrade.learner.db;

import com.pretrade.shared.models.StockAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StockAnalysisRepository extends JpaRepository<StockAnalysis, Long> {

    List<StockAnalysis> findByTradeDate(LocalDate tradeDate);
}
