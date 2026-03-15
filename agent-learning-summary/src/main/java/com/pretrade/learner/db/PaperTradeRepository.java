package com.pretrade.learner.db;

import com.pretrade.shared.models.PaperTrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaperTradeRepository extends JpaRepository<PaperTrade, Long> {

    List<PaperTrade> findByTradeDate(LocalDate tradeDate);

    List<PaperTrade> findByTradeDateAndStatus(LocalDate tradeDate, String status);

    List<PaperTrade> findByTradeDateBetween(LocalDate startDate, LocalDate endDate);
}
