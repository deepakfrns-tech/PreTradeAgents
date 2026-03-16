"""Analysis pipeline: collect NSE data → score with Claude → save to DB.

Orchestrates the full Market Analyst workflow:
1. Collect pre-market data (NIFTY 50 gap stocks)
2. Collect market snapshot (indices, VIX, advance/decline)
3. For F&O-eligible gap stocks: collect option chains + news
4. Send all data to Claude for scoring
5. Save StockAnalysis records to the database
"""

import json
import logging
import os
from dataclasses import asdict

import anthropic

from shared.database import SessionLocal
from shared.models import StockAnalysis, MarketSnapshot
from shared.time_utils import today_ist, now_ist
from shared.lot_sizes import is_valid_fno_stock, get_lot_size
from market_analyst.collectors.nse_collector import (
    collect_pre_market_data,
    collect_option_chain,
    collect_market_snapshot,
)
from market_analyst.collectors.news_collector import collect_news, collect_market_news
from market_analyst.collectors.technical_collector import collect_volume_data, collect_vwap_data

logger = logging.getLogger(__name__)

MIN_GAP_PERCENT = float(os.environ.get("MIN_GAP_PERCENT", "0.5"))
TOP_N_STOCKS = int(os.environ.get("TOP_N_STOCKS", "10"))

CLAUDE_MODEL = os.environ.get("CLAUDE_MODEL", "claude-sonnet-4-20250514")

SCORING_PROMPT = """You are a pre-market trading analyst for NSE F&O stocks. Analyze the data below and produce a JSON analysis for each stock.

## Market Snapshot
{market_snapshot}

## Market News
{market_news}

## Stocks to Analyze
{stocks_data}

For EACH stock, return a JSON object with these exact fields:
- symbol (string)
- gap_score (float 0-10): How tradeable is this gap? Consider gap size, volume, news catalyst.
- sentiment_score (float -1 to 1): -1 = very bearish, 0 = neutral, 1 = very bullish
- sentiment_level (string): one of "VERY_BEARISH", "BEARISH", "NEUTRAL", "BULLISH", "VERY_BULLISH"
- sentiment_reasoning (string): Brief explanation of sentiment assessment
- volume_score (float 0-10): Score based on pre-market volume activity
- oi_score (float 0-10): Score based on option chain data (PCR, OI buildup, IV)
- pcr (float): Put-Call ratio from option chain data
- oi_buildup (string): one of "LONG_BUILDUP", "SHORT_BUILDUP", "LONG_UNWINDING", "SHORT_COVERING", "NEUTRAL"
- max_pain (float): Max pain strike price from option chain
- iv_percentile (float 0-100): Implied volatility percentile estimate
- suggested_strike (float): Recommended strike price for the trade
- composite_score (float 0-100): Overall signal strength combining all factors
- signal_direction (string): "BULLISH" or "BEARISH"
- recommended_action (string): one of "STRONG_BUY", "BUY", "HOLD", "SELL", "STRONG_SELL"
- confidence_level (string): one of "HIGH", "MEDIUM", "LOW"
- claude_reasoning (string): Detailed reasoning for the recommendation
- risk_warnings (string): Key risks to watch
- entry_strike (float): Suggested option strike price
- estimated_premium (float): Estimated option premium in INR
- stop_loss (float): Stop loss level for the premium
- target (float): Target level for the premium
- risk_reward_ratio (float): Risk/reward ratio

Return ONLY a JSON array of objects. No markdown, no explanation outside the JSON."""


def run_pipeline(trade_date=None, min_gap=None, top_n=None):
    """Run the full analysis pipeline. Returns list of saved signal dicts."""
    trade_date = trade_date or today_ist()
    min_gap = min_gap if min_gap is not None else MIN_GAP_PERCENT
    top_n = top_n if top_n is not None else TOP_N_STOCKS

    logger.info("=== Starting analysis pipeline for %s ===", trade_date)

    # Step 1: Collect pre-market data
    logger.info("Step 1: Collecting pre-market data...")
    pre_market = collect_pre_market_data()
    if not pre_market:
        logger.warning("No pre-market data returned from NSE. Pipeline aborted.")
        return {"status": "error", "message": "No pre-market data from NSE. Market may be closed or NSE blocked the request.", "signals": []}

    # Step 2: Collect market snapshot
    logger.info("Step 2: Collecting market snapshot...")
    snapshot = collect_market_snapshot()
    market_news = collect_market_news()

    # Save market snapshot to DB
    _save_market_snapshot(trade_date, snapshot, pre_market)

    # Step 3: Filter for F&O-eligible gap stocks
    gap_stocks = []
    for entry in pre_market:
        if abs(entry.gap_percent) >= min_gap and is_valid_fno_stock(entry.symbol):
            gap_stocks.append(entry)

    # Sort by absolute gap percent descending, take top N
    gap_stocks.sort(key=lambda e: abs(e.gap_percent), reverse=True)
    gap_stocks = gap_stocks[:top_n]

    if not gap_stocks:
        logger.info("No F&O stocks with gap >= %.1f%%. Pipeline complete with 0 signals.", min_gap)
        return {"status": "ok", "message": f"No F&O stocks with gap >= {min_gap}%", "signals": []}

    logger.info("Step 3: Found %d gap stocks: %s", len(gap_stocks), [s.symbol for s in gap_stocks])

    # Step 4: Enrich each stock with option chain + news
    logger.info("Step 4: Enriching stocks with option chains and news...")
    stocks_data = []
    for entry in gap_stocks:
        oc = collect_option_chain(entry.symbol)
        news = collect_news(entry.symbol)
        vol = collect_volume_data(entry.symbol)
        vwap = collect_vwap_data(entry.symbol)

        stocks_data.append({
            "pre_market": asdict(entry),
            "option_chain": {
                "symbol": oc.symbol,
                "underlying_value": oc.underlying_value,
                "expiry_dates": oc.expiry_dates[:3],
                "entries_count": len(oc.entries),
                "near_atm_entries": _get_near_atm_entries(oc),
            },
            "news": [{"headline": n.headline, "source": n.source} for n in news[:10]],
            "volume": asdict(vol),
            "vwap": asdict(vwap),
            "lot_size": get_lot_size(entry.symbol),
        })

    # Step 5: Score with Claude
    logger.info("Step 5: Scoring with Claude (%s)...", CLAUDE_MODEL)
    scored = _score_with_claude(snapshot, market_news, stocks_data)
    if not scored:
        logger.error("Claude scoring returned no results.")
        return {"status": "error", "message": "Claude scoring failed. Check ANTHROPIC_API_KEY.", "signals": []}

    # Step 6: Save to database
    logger.info("Step 6: Saving %d signals to database...", len(scored))
    saved = _save_signals(trade_date, gap_stocks, scored)

    logger.info("=== Pipeline complete: %d signals saved for %s ===", len(saved), trade_date)
    return {"status": "ok", "message": f"Analyzed {len(saved)} stocks", "signals": saved}


def _get_near_atm_entries(oc, count=5):
    """Get option chain entries nearest to ATM for Claude analysis."""
    if not oc.entries or oc.underlying_value == 0:
        return []

    atm = oc.underlying_value
    sorted_entries = sorted(oc.entries, key=lambda e: abs(e.strike_price - atm))
    return [asdict(e) for e in sorted_entries[:count]]


def _score_with_claude(snapshot, market_news, stocks_data):
    """Send collected data to Claude for scoring."""
    api_key = os.environ.get("ANTHROPIC_API_KEY")
    if not api_key:
        logger.error("ANTHROPIC_API_KEY not set. Cannot score signals.")
        return []

    snapshot_text = json.dumps(asdict(snapshot), indent=2)
    news_text = json.dumps(
        [{"headline": n.headline, "source": n.source} for n in market_news[:15]],
        indent=2,
    )
    stocks_text = json.dumps(stocks_data, indent=2)

    prompt = SCORING_PROMPT.format(
        market_snapshot=snapshot_text,
        market_news=news_text,
        stocks_data=stocks_text,
    )

    try:
        client = anthropic.Anthropic(api_key=api_key)
        response = client.messages.create(
            model=CLAUDE_MODEL,
            max_tokens=4096,
            messages=[{"role": "user", "content": prompt}],
        )

        content = response.content[0].text.strip()
        # Strip markdown code fences if present
        if content.startswith("```"):
            content = content.split("\n", 1)[1]
            content = content.rsplit("```", 1)[0]

        scored = json.loads(content)
        if isinstance(scored, dict):
            scored = [scored]
        logger.info("Claude returned scores for %d stocks", len(scored))
        return scored
    except json.JSONDecodeError as e:
        logger.error("Failed to parse Claude response as JSON: %s", e)
        logger.error("Raw response: %s", content[:500] if 'content' in dir() else "N/A")
        return []
    except Exception as e:
        logger.error("Claude API call failed: %s", e)
        return []


def _save_signals(trade_date, gap_stocks, scored):
    """Save scored signals as StockAnalysis records."""
    # Index scored results by symbol for lookup
    scored_map = {s["symbol"]: s for s in scored if "symbol" in s}

    # Index gap stocks by symbol for pre-market data
    gap_map = {e.symbol: e for e in gap_stocks}

    db = SessionLocal()
    saved = []
    try:
        for symbol, score in scored_map.items():
            pre = gap_map.get(symbol)
            if not pre:
                continue

            gap_pct = pre.gap_percent
            gap_dir = "GAP_UP" if gap_pct > 0 else "GAP_DOWN"
            gap_cat = _categorize_gap(abs(gap_pct))

            # Upsert: update if exists, insert if not
            existing = (
                db.query(StockAnalysis)
                .filter(StockAnalysis.trade_date == trade_date, StockAnalysis.symbol == symbol)
                .first()
            )

            sa = existing if existing else StockAnalysis()
            if not existing:
                db.add(sa)

            sa.trade_date = trade_date
            sa.symbol = symbol
            sa.gap_percent = gap_pct
            sa.gap_direction = gap_dir
            sa.gap_category = gap_cat
            sa.atr_ratio = abs(gap_pct) / 1.5  # rough ATR estimate
            sa.gap_score = score.get("gap_score")
            sa.sentiment_score = score.get("sentiment_score")
            sa.sentiment_level = score.get("sentiment_level")
            sa.sentiment_reasoning = score.get("sentiment_reasoning")
            sa.volume_ratio = pre.final_quantity / max(1, pre.total_buy_quantity) if pre.total_buy_quantity else 0
            sa.volume_level = _volume_level(sa.volume_ratio)
            sa.volume_score = score.get("volume_score")
            sa.vwap_position = 0.0
            sa.pcr = score.get("pcr")
            sa.oi_buildup = score.get("oi_buildup")
            sa.max_pain = score.get("max_pain")
            sa.iv_percentile = score.get("iv_percentile")
            sa.suggested_strike = score.get("suggested_strike")
            sa.oi_score = score.get("oi_score")
            sa.composite_score = score.get("composite_score")
            sa.signal_direction = score.get("signal_direction")
            sa.recommended_action = score.get("recommended_action")
            sa.claude_reasoning = score.get("claude_reasoning")
            sa.risk_warnings = score.get("risk_warnings")
            sa.confidence_level = score.get("confidence_level")
            sa.entry_strike = score.get("entry_strike")
            sa.estimated_premium = score.get("estimated_premium")
            sa.stop_loss = score.get("stop_loss")
            sa.target = score.get("target")
            sa.risk_reward_ratio = score.get("risk_reward_ratio")
            sa.headline_details = json.dumps(
                [{"headline": n.headline, "source": n.source}
                 for entry in gap_stocks if entry.symbol == symbol
                 for n in collect_news(symbol)[:5]]
            ) if not existing else existing.headline_details

            saved.append(sa)

        db.commit()

        # Refresh to get IDs and return dicts
        result = []
        for sa in saved:
            db.refresh(sa)
            result.append(sa.to_dict())

        return result
    except Exception as e:
        db.rollback()
        logger.error("Failed to save signals: %s", e)
        return []
    finally:
        db.close()


def _save_market_snapshot(trade_date, snapshot, pre_market):
    """Save market snapshot to DB."""
    db = SessionLocal()
    try:
        existing = db.query(MarketSnapshot).filter(MarketSnapshot.trade_date == trade_date).first()
        ms = existing if existing else MarketSnapshot()
        if not existing:
            db.add(ms)

        nifty_gaps = [e.gap_percent for e in pre_market if e.symbol in ("NIFTY 50", "NIFTY")]
        avg_gap = sum(e.gap_percent for e in pre_market) / max(1, len(pre_market))

        ms.trade_date = trade_date
        ms.timestamp = now_ist().replace(tzinfo=None)
        ms.nifty_gap_percent = nifty_gaps[0] if nifty_gaps else avg_gap
        ms.india_vix = snapshot.india_vix
        ms.advance_decline_ratio = snapshot.advance_decline_ratio
        ms.market_sentiment = _market_sentiment(avg_gap, snapshot.advance_decline_ratio)
        ms.raw_pre_market_data = json.dumps([asdict(e) for e in pre_market[:20]])

        db.commit()
        logger.info("Market snapshot saved for %s", trade_date)
    except Exception as e:
        db.rollback()
        logger.error("Failed to save market snapshot: %s", e)
    finally:
        db.close()


def _categorize_gap(abs_gap):
    if abs_gap >= 3.0:
        return "HUGE_GAP"
    elif abs_gap >= 2.0:
        return "LARGE_GAP"
    elif abs_gap >= 1.0:
        return "MODERATE_GAP"
    else:
        return "SMALL_GAP"


def _volume_level(ratio):
    if ratio >= 2.5:
        return "VERY_HIGH"
    elif ratio >= 1.5:
        return "HIGH"
    elif ratio >= 0.8:
        return "NORMAL"
    else:
        return "LOW"


def _market_sentiment(avg_gap, adr):
    if avg_gap > 1.0 and adr > 1.5:
        return "STRONGLY_BULLISH"
    elif avg_gap > 0.3 and adr > 1.0:
        return "BULLISH"
    elif avg_gap < -1.0 and adr < 0.67:
        return "STRONGLY_BEARISH"
    elif avg_gap < -0.3 and adr < 1.0:
        return "BEARISH"
    return "NEUTRAL"
