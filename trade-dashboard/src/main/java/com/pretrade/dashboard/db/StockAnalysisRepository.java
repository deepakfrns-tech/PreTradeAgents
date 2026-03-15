package com.pretrade.dashboard.db;

import com.pretrade.shared.models.StockAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockAnalysisRepository extends JpaRepository<StockAnalysis, Long> {

    List<StockAnalysis> findByTradeDateOrderByCompositeScoreDesc(LocalDate tradeDate);

    Optional<StockAnalysis> findByTradeDateAndSymbol(LocalDate tradeDate, String symbol);

    List<StockAnalysis> findAllByOrderByTradeDateDescCompositeScoreDesc();
}
