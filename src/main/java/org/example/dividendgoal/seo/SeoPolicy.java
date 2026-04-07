package org.example.dividendgoal.seo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Central SEO policy so sitemap generation and robots rules do not diverge.
 */
public final class SeoPolicy {

    private static final List<Integer> INDEXABLE_TARGET_AMOUNTS = List.of(1000, 2000);

    private static final List<String> INDEXABLE_TARGET_TICKERS = List.of(
            "SCHD", "JEPI", "JEPQ", "VTI");

    private static final List<String> INDEXABLE_LIFESTYLE_ITEMS = List.of();

    private static final List<ComparisonSpotlight> CURATED_COMPARISON_SPOTLIGHTS = List.of(
            spotlight("JEPI", "JEPQ", "Income ETF showdown",
                    "Two covered-call ETFs with different sector bets and upside ceilings.", "Dividend ETF"),
            spotlight("JEPI", "SCHD", "Income now vs dividend quality",
                    "Use this page when you are choosing between headline yield and a cleaner dividend profile.",
                    "Dividend ETF"),
            spotlight("JEPQ", "SCHD", "Tech upside vs dividend discipline",
                    "A high-distribution Nasdaq strategy against a quality dividend core holding.", "Dividend ETF"),
            spotlight("SCHD", "VYM", "Quality screen vs broad dividend basket",
                    "Compare concentration, yield quality, and diversification inside large-cap dividend ETFs.",
                    "Dividend ETF"),
            spotlight("SCHD", "VTI", "Dividend strategy vs total market",
                    "Decide whether you want explicit income tilts or a market-wide core position.", "Dividend ETF"),
            spotlight("DGRO", "HDV", "Dividend growth vs high yield",
                    "One fund leans toward payout growth, the other toward current income and defensiveness.",
                    "Dividend ETF"),
            spotlight("SPHD", "VYM", "Low-volatility income vs broad dividend exposure",
                    "For investors who want monthly distributions without giving up all diversification.",
                    "Dividend ETF"),
            spotlight("O", "SCHD", "REIT monthly income vs ETF simplicity",
                    "A single monthly payer against a diversified dividend ETF with lower company-specific risk.",
                    "Dividend ETF"),
            spotlight("CVX", "XOM", "Oil major dividend resilience",
                    "Track the two biggest integrated energy dividend names side by side.", "Dividend Stock"),
            spotlight("KO", "PEP", "Consumer staples dividend duel",
                    "A classic comparison for investors who want defensive cash flow with pricing power.",
                    "Dividend Stock"),
            spotlight("JNJ", "PFE", "Pharma quality vs turnaround risk",
                    "Healthcare income comparison with a clear balance-sheet and growth tradeoff.", "Dividend Stock"),
            spotlight("LMT", "RTX", "Defense dividend leaders",
                    "Compare backlog quality, yield, and execution risk across the defense sector.", "Dividend Stock"),
            spotlight("AAPL", "MSFT", "Big Tech dividends for total-return investors",
                    "Not a high-yield matchup, but still one of the few recurring stock-comparison intents left alive.",
                    "Dividend Stock"));

    private static final List<String> CORE_STATIC_PATHS = List.of(
            "/",
            "/articles",
            "/about",
            "/methodology");

    private static final List<String> ARTICLE_SLUGS = List.of(
            "what-is-dividend-yield",
            "schd-vs-jepi-comparison",
            "dividend-income-vs-interest",
            "best-monthly-dividend-stocks");

    private static final Set<String> INDEXABLE_TARGET_TICKER_SET = uppercaseSet(INDEXABLE_TARGET_TICKERS);
    private static final Set<String> INDEXABLE_LIFESTYLE_ITEM_SET = lowercaseSet(INDEXABLE_LIFESTYLE_ITEMS);
    private static final Set<String> INDEXABLE_COMPARISON_PAIR_KEYS = buildComparisonPairKeys();

    private SeoPolicy() {
    }

    public static List<Integer> getIndexableTargetAmounts() {
        return INDEXABLE_TARGET_AMOUNTS;
    }

    public static List<String> getCoreStaticPaths() {
        return CORE_STATIC_PATHS;
    }

    public static List<String> getArticleSlugs() {
        return ARTICLE_SLUGS;
    }

    public static List<String> getIndexableLifestyleItemSlugs() {
        return INDEXABLE_LIFESTYLE_ITEMS;
    }

    public static List<String> getIndexableTickersForTargets(Collection<String> availableTickers) {
        return filterAvailableTickers(INDEXABLE_TARGET_TICKERS, availableTickers);
    }

    public static List<String> getIndexableTickersForLifestyle(Collection<String> availableTickers) {
        return List.of();
    }

    public static List<ComparisonPair> getIndexableComparisonPairs(Collection<String> availableTickers) {
        return getComparisonSpotlights(availableTickers).stream()
                .map(spotlight -> new ComparisonPair(spotlight.left(), spotlight.right()))
                .toList();
    }

    public static List<ComparisonSpotlight> getComparisonSpotlights(Collection<String> availableTickers) {
        Set<String> availableSet = uppercaseSet(availableTickers);
        return CURATED_COMPARISON_SPOTLIGHTS.stream()
                .filter(spotlight -> availableSet.contains(spotlight.left()) && availableSet.contains(spotlight.right()))
                .toList();
    }

    public static Optional<ComparisonSpotlight> findComparisonSpotlight(String ticker1, String ticker2) {
        String pairKey = comparisonPairKey(ticker1, ticker2);
        return CURATED_COMPARISON_SPOTLIGHTS.stream()
                .filter(spotlight -> spotlight.pairKey().equals(pairKey))
                .findFirst();
    }

    public static List<ComparisonSpotlight> getRelatedComparisonSpotlights(
            String ticker,
            Collection<String> availableTickers,
            int limit) {
        return getRelatedComparisonSpotlights(ticker, null, availableTickers, limit);
    }

    public static List<ComparisonSpotlight> getRelatedComparisonSpotlights(
            String primaryTicker,
            String secondaryTicker,
            String excludedPairKey,
            Collection<String> availableTickers,
            int limit) {
        String normalizedPrimary = normalizeTicker(primaryTicker);
        String normalizedSecondary = normalizeTicker(secondaryTicker);
        List<ComparisonSpotlight> availableSpotlights = getComparisonSpotlights(availableTickers);
        List<ComparisonSpotlight> prioritized = new ArrayList<>();

        for (ComparisonSpotlight spotlight : availableSpotlights) {
            if (spotlight.pairKey().equals(excludedPairKey)) {
                continue;
            }

            boolean matchesPrimary = spotlight.left().equals(normalizedPrimary)
                    || spotlight.right().equals(normalizedPrimary);
            boolean matchesSecondary = spotlight.left().equals(normalizedSecondary)
                    || spotlight.right().equals(normalizedSecondary);

            if (matchesPrimary || matchesSecondary) {
                prioritized.add(spotlight);
            }
        }

        for (ComparisonSpotlight spotlight : availableSpotlights) {
            if (spotlight.pairKey().equals(excludedPairKey) || prioritized.contains(spotlight)) {
                continue;
            }
            prioritized.add(spotlight);
        }

        return prioritized.subList(0, Math.min(limit, prioritized.size()));
    }

    public static List<ComparisonSpotlight> getRelatedComparisonSpotlights(
            String ticker,
            String excludedPairKey,
            Collection<String> availableTickers,
            int limit) {
        String normalizedTicker = normalizeTicker(ticker);
        List<ComparisonSpotlight> availableSpotlights = getComparisonSpotlights(availableTickers);
        List<ComparisonSpotlight> prioritized = new ArrayList<>();

        for (ComparisonSpotlight spotlight : availableSpotlights) {
            if (spotlight.pairKey().equals(excludedPairKey)) {
                continue;
            }
            if (spotlight.left().equals(normalizedTicker) || spotlight.right().equals(normalizedTicker)) {
                prioritized.add(spotlight);
            }
        }

        for (ComparisonSpotlight spotlight : availableSpotlights) {
            if (spotlight.pairKey().equals(excludedPairKey) || prioritized.contains(spotlight)) {
                continue;
            }
            prioritized.add(spotlight);
        }

        return prioritized.subList(0, Math.min(limit, prioritized.size()));
    }

    public static boolean isIndexableTargetPage(String ticker, double monthlyAmount) {
        int normalizedAmount = (int) monthlyAmount;
        return monthlyAmount == normalizedAmount
                && INDEXABLE_TARGET_AMOUNTS.contains(normalizedAmount)
                && INDEXABLE_TARGET_TICKER_SET.contains(normalizeTicker(ticker));
    }

    public static boolean isIndexableTargetTicker(String ticker) {
        return INDEXABLE_TARGET_TICKER_SET.contains(normalizeTicker(ticker));
    }

    public static boolean isIndexableIncomePage(String ticker, double capital) {
        return false;
    }

    public static boolean isIndexableLifestyleHubPage(String itemSlug) {
        return INDEXABLE_LIFESTYLE_ITEM_SET.contains(normalizeSlug(itemSlug));
    }

    public static boolean isIndexableLifestylePage(String itemSlug, String ticker) {
        return INDEXABLE_LIFESTYLE_ITEM_SET.contains(normalizeSlug(itemSlug))
                && INDEXABLE_TARGET_TICKER_SET.contains(normalizeTicker(ticker));
    }

    public static boolean isIndexableComparisonPage(String ticker1, String ticker2) {
        return INDEXABLE_COMPARISON_PAIR_KEYS.contains(comparisonPairKey(ticker1, ticker2));
    }

    public static ComparisonPair canonicalComparisonPair(String ticker1, String ticker2) {
        String first = normalizeTicker(ticker1);
        String second = normalizeTicker(ticker2);
        if (first.compareTo(second) <= 0) {
            return new ComparisonPair(first, second);
        }
        return new ComparisonPair(second, first);
    }

    private static List<String> filterAvailableTickers(List<String> preferredTickers, Collection<String> availableTickers) {
        Set<String> availableSet = uppercaseSet(availableTickers);
        List<String> filtered = new ArrayList<>();
        for (String ticker : preferredTickers) {
            if (availableSet.contains(ticker)) {
                filtered.add(ticker);
            }
        }
        return filtered;
    }

    private static Set<String> buildComparisonPairKeys() {
        Set<String> keys = new LinkedHashSet<>();
        for (ComparisonSpotlight spotlight : CURATED_COMPARISON_SPOTLIGHTS) {
            keys.add(spotlight.pairKey());
        }
        return Set.copyOf(keys);
    }

    private static ComparisonSpotlight spotlight(String ticker1, String ticker2, String title, String summary, String category) {
        ComparisonPair pair = canonicalComparisonPair(ticker1, ticker2);
        String label = pair.left() + " vs " + pair.right();
        String path = "/compare/" + pair.left() + "-vs-" + pair.right();
        String pairKey = pair.left() + "::" + pair.right();
        return new ComparisonSpotlight(pair.left(), pair.right(), label, path, title, summary, category, pairKey);
    }

    private static String comparisonPairKey(String ticker1, String ticker2) {
        ComparisonPair pair = canonicalComparisonPair(ticker1, ticker2);
        return pair.left() + "::" + pair.right();
    }

    private static Set<String> uppercaseSet(Collection<String> values) {
        Set<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            normalized.add(normalizeTicker(value));
        }
        return Set.copyOf(normalized);
    }

    private static Set<String> lowercaseSet(Collection<String> values) {
        Set<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            normalized.add(normalizeSlug(value));
        }
        return Set.copyOf(normalized);
    }

    private static String normalizeTicker(String ticker) {
        return ticker == null ? "" : ticker.trim().toUpperCase(Locale.US);
    }

    private static String normalizeSlug(String slug) {
        return slug == null ? "" : slug.trim().toLowerCase(Locale.US);
    }

    public record ComparisonPair(String left, String right) {
    }

    public record ComparisonSpotlight(
            String left,
            String right,
            String label,
            String path,
            String title,
            String summary,
            String category,
            String pairKey) {
    }
}
