package com.pretrade.executor.db;

import com.pretrade.shared.models.TradeDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TradeDecisionRepository extends JpaRepository<TradeDecision, Long> {

    List<TradeDecision> findByTradeDateAndDecision(LocalDate tradeDate, String decision);
}
