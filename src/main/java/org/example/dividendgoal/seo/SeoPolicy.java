package org.example.dividendgoal.seo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Central SEO policy so sitemap generation and robots rules do not diverge.
 */
public final class SeoPolicy {

    private static final List<Integer> INDEXABLE_TARGET_AMOUNTS = List.of(500, 1000, 2000, 5000);

    private static final List<String> INDEXABLE_TARGET_TICKERS = List.of(
            "SCHD", "VYM", "JEPI", "O", "VTI", "QYLD");

    private static final List<String> INDEXABLE_LIFESTYLE_ITEMS = List.of(
            "netflix-premium",
            "amazon-prime",
            "iphone-16-pro-installment",
            "tesla-model-3-lease",
            "starbucks-daily",
            "avg-rent-texas");

    private static final List<String> INDEXABLE_COMPARISON_TICKERS = List.of(
            "SCHD", "VYM", "JEPI", "O", "VTI", "QYLD");

    private static final List<String> CORE_STATIC_PATHS = List.of(
            "/",
            "/articles",
            "/about",
            "/methodology");

    private static final List<String> ARTICLE_SLUGS = List.of(
            "what-is-dividend-yield",
            "why-dividend-growth-matters",
            "schd-vs-jepi-comparison",
            "how-to-use-dividend-calculator",
            "dividend-income-vs-interest",
            "why-small-expenses-matter",
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
        return filterAvailableTickers(INDEXABLE_TARGET_TICKERS, availableTickers);
    }

    public static List<ComparisonPair> getIndexableComparisonPairs(Collection<String> availableTickers) {
        List<String> tickers = filterAvailableTickers(INDEXABLE_COMPARISON_TICKERS, availableTickers);
        List<ComparisonPair> pairs = new ArrayList<>();

        for (int i = 0; i < tickers.size(); i++) {
            for (int j = i + 1; j < tickers.size(); j++) {
                pairs.add(canonicalComparisonPair(tickers.get(i), tickers.get(j)));
            }
        }

        return pairs;
    }

    public static boolean isIndexableTargetPage(String ticker, double monthlyAmount) {
        int normalizedAmount = (int) monthlyAmount;
        return monthlyAmount == normalizedAmount
                && INDEXABLE_TARGET_AMOUNTS.contains(normalizedAmount)
                && INDEXABLE_TARGET_TICKER_SET.contains(normalizeTicker(ticker));
    }

    public static boolean isIndexableIncomePage(String ticker, double capital) {
        return false;
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
        for (int i = 0; i < INDEXABLE_COMPARISON_TICKERS.size(); i++) {
            for (int j = i + 1; j < INDEXABLE_COMPARISON_TICKERS.size(); j++) {
                keys.add(comparisonPairKey(INDEXABLE_COMPARISON_TICKERS.get(i), INDEXABLE_COMPARISON_TICKERS.get(j)));
            }
        }
        return Set.copyOf(keys);
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
}
