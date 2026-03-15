package com.pretrade.executor.db;

import com.pretrade.shared.models.PaperTrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaperTradeRepository extends JpaRepository<PaperTrade, Long> {

    List<PaperTrade> findByTradeDateAndStatus(LocalDate tradeDate, String status);

    List<PaperTrade> findByTradeDate(LocalDate tradeDate);

    long countByTradeDateAndStatus(LocalDate tradeDate, String status);
}
