"""Collects news headlines from Google News RSS and MoneyControl.

WARNING: External site changes can break parsing at any time.
"""

import logging
import re
from dataclasses import dataclass

import requests

logger = logging.getLogger(__name__)

GOOGLE_NEWS_RSS = "https://news.google.com/rss/search?q=%s+stock+NSE&hl=en-IN&gl=IN&ceid=IN:en"
MONEYCONTROL_SEARCH = "https://www.moneycontrol.com/stocks/cptmarket/compsearchnew.php?search_data=%s&cid=&mbession=&tession=&search_type=SC"

TITLE_PATTERN = re.compile(r"<title><!\[CDATA\[(.+?)]]></title>")
PUB_DATE_PATTERN = re.compile(r"<pubDate>(.+?)</pubDate>")
MC_TITLE_PATTERN = re.compile(r'<a[^>]*class="[^"]*news[^"]*"[^>]*>([^<]+)</a>')

USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
DEFAULT_TIMEOUT = 30


@dataclass
class NewsItem:
    headline: str = ""
    source: str = ""
    published_at: str = ""
    url: str = ""


def collect_news(symbol: str, timeout: int = DEFAULT_TIMEOUT) -> list[NewsItem]:
    """Collects news headlines for a specific stock symbol from multiple sources."""
    logger.info("Collecting news for symbol: %s", symbol)
    all_news = []

    try:
        all_news.extend(_fetch_google_news(symbol, timeout))
    except Exception as e:
        logger.warning("Failed to fetch Google News for %s: %s", symbol, e)

    try:
        all_news.extend(_fetch_moneycontrol_news(symbol, timeout))
    except Exception as e:
        logger.warning("Failed to fetch MoneyControl news for %s: %s", symbol, e)

    logger.info("Collected %d news items for %s", len(all_news), symbol)
    return all_news


def collect_market_news(timeout: int = DEFAULT_TIMEOUT) -> list[NewsItem]:
    """Collects broad market news headlines."""
    logger.info("Collecting broad market news...")
    all_news = []
    for query in ["Nifty+market", "Indian+stock+market", "RBI+monetary+policy"]:
        try:
            all_news.extend(_fetch_google_news_raw(query, timeout))
        except Exception as e:
            logger.warning("Failed to fetch market news for query '%s': %s", query, e)

    logger.info("Collected %d market news items", len(all_news))
    return all_news


def _fetch_google_news(symbol: str, timeout: int) -> list[NewsItem]:
    url = GOOGLE_NEWS_RSS % symbol
    return _fetch_google_news_raw(url, timeout)


def _fetch_google_news_raw(query_or_url: str, timeout: int) -> list[NewsItem]:
    url = query_or_url if query_or_url.startswith("http") else GOOGLE_NEWS_RSS % query_or_url
    headers = {"User-Agent": USER_AGENT, "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9"}
    response = requests.get(url, headers=headers, timeout=timeout)
    response.raise_for_status()
    return _parse_rss(response.text, "GoogleNews")


def _fetch_moneycontrol_news(symbol: str, timeout: int) -> list[NewsItem]:
    headers = {"User-Agent": USER_AGENT, "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9"}
    response = requests.get(MONEYCONTROL_SEARCH % symbol, headers=headers, timeout=timeout)
    response.raise_for_status()
    return _parse_moneycontrol(response.text, symbol)


def _parse_rss(rss_xml: str, source: str) -> list[NewsItem]:
    items = []
    if not rss_xml:
        return items

    titles = TITLE_PATTERN.findall(rss_xml)
    dates = PUB_DATE_PATTERN.findall(rss_xml)

    # Skip the channel title (first match)
    for i, title in enumerate(titles[1:], start=0):
        pub_date = dates[i].strip() if i < len(dates) else ""
        items.append(NewsItem(headline=title.strip(), source=source, published_at=pub_date))
        if len(items) >= 15:
            break

    return items


def _parse_moneycontrol(html: str, symbol: str) -> list[NewsItem]:
    items = []
    if not html:
        return items

    for match in MC_TITLE_PATTERN.finditer(html):
        title = match.group(1).strip()
        if title and len(title) > 10:
            items.append(NewsItem(headline=title, source="MoneyControl"))
        if len(items) >= 10:
            break

    return items
