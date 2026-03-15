package com.pretrade.analyst.collectors;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.pretrade.analyst.config.AnalystSettings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class NewsCollector {

    private static final String GOOGLE_NEWS_RSS = "https://news.google.com/rss/search?q=%s+stock+NSE&hl=en-IN&gl=IN&ceid=IN:en";
    private static final String MONEYCONTROL_SEARCH = "https://www.moneycontrol.com/stocks/cptmarket/compsearchnew.php?search_data=%s&cid=&mbession=&tession=&search_type=SC";
    private static final Pattern TITLE_PATTERN = Pattern.compile("<title><!\\[CDATA\\[(.+?)]]></title>");
    private static final Pattern PUB_DATE_PATTERN = Pattern.compile("<pubDate>(.+?)</pubDate>");

    private final AnalystSettings settings;

    private WebClient buildWebClient() {
        return WebClient.builder()
                .defaultHeader("User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .defaultHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9")
                .build();
    }

    /**
     * Collects news headlines for a specific stock symbol from multiple sources.
     */
    public List<NewsItem> collectNews(String symbol) {
        log.info("Collecting news for symbol: {}", symbol);
        List<NewsItem> allNews = new ArrayList<>();

        try {
            List<NewsItem> googleNews = fetchGoogleNews(symbol);
            allNews.addAll(googleNews);
        } catch (Exception e) {
            log.warn("Failed to fetch Google News for {}: {}", symbol, e.getMessage());
        }

        try {
            List<NewsItem> mcNews = fetchMoneyControlNews(symbol);
            allNews.addAll(mcNews);
        } catch (Exception e) {
            log.warn("Failed to fetch MoneyControl news for {}: {}", symbol, e.getMessage());
        }

        log.info("Collected {} news items for {}", allNews.size(), symbol);
        return allNews;
    }

    /**
     * Collects broad market news headlines.
     */
    public List<NewsItem> collectMarketNews() {
        log.info("Collecting broad market news...");
        List<NewsItem> allNews = new ArrayList<>();

        for (String query : List.of("Nifty+market", "Indian+stock+market", "RBI+monetary+policy")) {
            try {
                allNews.addAll(fetchGoogleNewsRaw(query));
            } catch (Exception e) {
                log.warn("Failed to fetch market news for query '{}': {}", query, e.getMessage());
            }
        }

        log.info("Collected {} market news items", allNews.size());
        return allNews;
    }

    private List<NewsItem> fetchGoogleNews(String symbol) {
        String url = String.format(GOOGLE_NEWS_RSS, symbol);
        return fetchGoogleNewsRaw(url);
    }

    private List<NewsItem> fetchGoogleNewsRaw(String queryOrUrl) {
        String url = queryOrUrl.startsWith("http") ? queryOrUrl
                : String.format(GOOGLE_NEWS_RSS, queryOrUrl);

        WebClient client = buildWebClient();
        String rssXml = client.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(settings.getDataSourceTimeoutSeconds()))
                .block();

        return parseRssResponse(rssXml, "GoogleNews");
    }

    private List<NewsItem> fetchMoneyControlNews(String symbol) {
        WebClient client = buildWebClient();
        String html = client.get()
                .uri(String.format(MONEYCONTROL_SEARCH, symbol))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(settings.getDataSourceTimeoutSeconds()))
                .block();

        return parseMoneyControlResponse(html, symbol);
    }

    private List<NewsItem> parseRssResponse(String rssXml, String source) {
        List<NewsItem> items = new ArrayList<>();
        if (rssXml == null || rssXml.isEmpty()) return items;

        try {
            Matcher titleMatcher = TITLE_PATTERN.matcher(rssXml);
            Matcher dateMatcher = PUB_DATE_PATTERN.matcher(rssXml);

            // Skip the channel title
            if (titleMatcher.find()) { /* skip */ }

            while (titleMatcher.find()) {
                String title = titleMatcher.group(1).trim();
                String pubDate = dateMatcher.find() ? dateMatcher.group(1).trim() : "";

                items.add(NewsItem.builder()
                        .headline(title)
                        .source(source)
                        .publishedAt(pubDate)
                        .build());

                if (items.size() >= 15) break; // Cap per source
            }
        } catch (Exception e) {
            log.error("Error parsing RSS response: {}", e.getMessage(), e);
        }
        return items;
    }

    private List<NewsItem> parseMoneyControlResponse(String html, String symbol) {
        List<NewsItem> items = new ArrayList<>();
        if (html == null || html.isEmpty()) return items;

        try {
            // Extract news titles from MoneyControl search results page
            Pattern mcTitlePattern = Pattern.compile("<a[^>]*class=\"[^\"]*news[^\"]*\"[^>]*>([^<]+)</a>");
            Matcher matcher = mcTitlePattern.matcher(html);

            while (matcher.find()) {
                String title = matcher.group(1).trim();
                if (!title.isEmpty() && title.length() > 10) {
                    items.add(NewsItem.builder()
                            .headline(title)
                            .source("MoneyControl")
                            .publishedAt("")
                            .build());
                }
                if (items.size() >= 10) break;
            }
        } catch (Exception e) {
            log.error("Error parsing MoneyControl response for {}: {}", symbol, e.getMessage(), e);
        }
        return items;
    }

    // --- DTOs ---

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewsItem {
        private String headline;
        private String source;
        private String publishedAt;
        private String url;
    }
}
