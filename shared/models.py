from sqlalchemy import (
    Column, BigInteger, String, Float, Date, DateTime, Text,
    Boolean, Integer, UniqueConstraint, ForeignKey,
)
from sqlalchemy.dialects.postgresql import JSONB
from sqlalchemy.orm import declarative_base, relationship

Base = declarative_base()


class MarketSnapshot(Base):
    __tablename__ = "market_snapshots"
    id = Column(BigInteger, primary_key=True, autoincrement=True)
    trade_date = Column(Date, index=True)
    timestamp = Column(DateTime)
    nifty_gap_percent = Column(Float)
    market_sentiment = Column(String(255))
    india_vix = Column(Float)
    advance_decline_ratio = Column(Float)
    fii_dii_data = Column(JSONB)
    raw_pre_market_data = Column(JSONB)

    def to_dict(self):
        return {c.name: getattr(self, c.name) for c in self.__table__.columns}


class StockAnalysis(Base):
    __tablename__ = "stock_analysis"
    __table_args__ = (UniqueConstraint("trade_date", "symbol"),)

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    trade_date = Column(Date, index=True)
    symbol = Column(String(255), index=True)
    gap_percent = Column(Float)
    gap_direction = Column(String(255))
    atr_ratio = Column(Float)
    gap_category = Column(String(255))
    gap_score = Column(Float)
    sentiment_score = Column(Float)
    sentiment_level = Column(String(255))
    sentiment_reasoning = Column(Text)
    headline_details = Column(JSONB)
    volume_ratio = Column(Float)
    volume_level = Column(String(255))
    vwap_position = Column(Float)
    volume_score = Column(Float)
    pcr = Column(Float)
    oi_buildup = Column(String(255))
    max_pain = Column(Float)
    iv_percentile = Column(Float)
    suggested_strike = Column(Float)
    oi_score = Column(Float)
    composite_score = Column(Float)
    signal_direction = Column(String(255))
    recommended_action = Column(String(255))
    claude_reasoning = Column(Text)
    risk_warnings = Column(Text)
    confidence_level = Column(String(255))
    entry_strike = Column(Float)
    estimated_premium = Column(Float)
    stop_loss = Column(Float)
    target = Column(Float)
    risk_reward_ratio = Column(Float)

    def to_dict(self):
        return {c.name: getattr(self, c.name) for c in self.__table__.columns}


class TradeDecision(Base):
    __tablename__ = "trade_decisions"
    __table_args__ = (UniqueConstraint("trade_date", "signal_id"),)

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    trade_date = Column(Date, index=True)
    signal_id = Column(BigInteger, ForeignKey("stock_analysis.id"), index=True)
    symbol = Column(String(255))
    decision = Column(String(255))
    decision_time = Column(DateTime)
    decision_reason = Column(Text)
    override_strike = Column(Float)
    override_sl = Column(Float)
    override_target = Column(Float)
    override_lot_size = Column(Integer)
    override_direction = Column(String(255))
    final_strike = Column(Float)
    final_sl = Column(Float)
    final_target = Column(Float)
    final_lot_size = Column(Integer)
    final_direction = Column(String(255))

    signal = relationship("StockAnalysis", lazy="joined")

    def to_dict(self):
        return {c.name: getattr(self, c.name) for c in self.__table__.columns}


class PaperTrade(Base):
    __tablename__ = "paper_trades"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    trade_date = Column(Date, index=True)
    symbol = Column(String(255), index=True)
    signal_id = Column(BigInteger, ForeignKey("stock_analysis.id"))
    direction = Column(String(255))
    strike_price = Column(Float)
    entry_price = Column(Float)
    entry_time = Column(DateTime)
    lot_size = Column(Integer)
    entry_reasoning = Column(Text)
    exit_price = Column(Float)
    exit_time = Column(DateTime)
    exit_reason = Column(String(255))
    exit_reasoning = Column(Text)
    pnl_amount = Column(Float)
    pnl_percent = Column(Float)
    max_profit = Column(Float)
    max_drawdown = Column(Float)
    price_trail = Column(JSONB)
    sl_adjustments = Column(JSONB)
    status = Column(String(255), index=True)
    created_at = Column(DateTime)

    def to_dict(self):
        return {c.name: getattr(self, c.name) for c in self.__table__.columns}


class DailySummary(Base):
    __tablename__ = "daily_summaries"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    trade_date = Column(Date, unique=True, index=True)
    total_trades = Column(Integer)
    winning_trades = Column(Integer)
    losing_trades = Column(Integer)
    win_rate = Column(Float)
    total_pnl = Column(Float)
    profit_factor = Column(Float)
    max_drawdown = Column(Float)
    sharpe_ratio = Column(Float)
    market_regime = Column(String(255))
    daily_narrative = Column(Text)
    trade_postmortems = Column(JSONB)
    signals_shown = Column(Integer)
    signals_approved = Column(Integer)
    signals_skipped = Column(Integer)
    approval_accuracy = Column(Float)
    skip_accuracy = Column(Float)
    override_impact = Column(JSONB)
    decision_narrative = Column(Text)
    missed_opportunities = Column(JSONB)
    patterns_identified = Column(JSONB)
    recommended_adjustments = Column(JSONB)
    created_at = Column(DateTime)

    def to_dict(self):
        return {c.name: getattr(self, c.name) for c in self.__table__.columns}


class StrategyLearning(Base):
    __tablename__ = "strategy_learnings"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    learning_date = Column(Date, index=True)
    category = Column(String(255), index=True)
    insight = Column(Text)
    evidence = Column(JSONB)
    confidence = Column(Float)
    times_validated = Column(Integer, default=0)
    is_active = Column(Boolean, default=True, index=True)
    created_at = Column(DateTime)

    def to_dict(self):
        return {c.name: getattr(self, c.name) for c in self.__table__.columns}
