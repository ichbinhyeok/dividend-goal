package org.example.dividendgoal.seo;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeoPolicyTest {

    @Test
    void targetPagesAreLimitedToCuratedTickerAndAmountCombinations() {
        assertTrue(SeoPolicy.isIndexableTargetPage("SCHD", 1000));
        assertTrue(SeoPolicy.isIndexableTargetPage("vti", 2000));
        assertTrue(SeoPolicy.isIndexableTargetTicker("SCHD"));
        assertTrue(SeoPolicy.isIndexableTargetTicker("JEPQ"));

        assertFalse(SeoPolicy.isIndexableTargetPage("ABBV", 1000));
        assertFalse(SeoPolicy.isIndexableTargetPage("SCHD", 500));
        assertFalse(SeoPolicy.isIndexableTargetPage("SCHD", 1000.5));
        assertFalse(SeoPolicy.isIndexableTargetTicker("VYM"));
    }

    @Test
    void incomePagesAreAlwaysNoindex() {
        assertFalse(SeoPolicy.isIndexableIncomePage("SCHD", 100000));
    }

    @Test
    void lifestylePagesAreRemovedFromCanonicalSurface() {
        assertFalse(SeoPolicy.isIndexableLifestyleHubPage("netflix-premium"));
        assertFalse(SeoPolicy.isIndexableLifestylePage("netflix-premium", "SCHD"));
        assertTrue(SeoPolicy.getIndexableLifestyleItemSlugs().isEmpty());
    }

    @Test
    void comparisonPagesUseCanonicalAlphabeticalOrder() {
        SeoPolicy.ComparisonPair pair = SeoPolicy.canonicalComparisonPair("XOM", "CVX");

        assertEquals("CVX", pair.left());
        assertEquals("XOM", pair.right());
        assertTrue(SeoPolicy.isIndexableComparisonPage("XOM", "CVX"));
        assertFalse(SeoPolicy.isIndexableComparisonPage("ABBV", "SCHD"));
    }

    @Test
    void comparisonSitemapOnlyContainsAvailableManualPairs() {
        List<SeoPolicy.ComparisonPair> pairs = SeoPolicy.getIndexableComparisonPairs(
                List.of("AAPL", "MSFT", "CVX", "XOM", "DGRO", "HDV", "JEPI", "JEPQ", "JNJ", "KO",
                        "LMT", "O", "PEP", "PFE", "RTX", "SCHD", "SPHD", "VTI", "VYM"));

        assertEquals(13, pairs.size());
        assertEquals(new SeoPolicy.ComparisonPair("JEPI", "JEPQ"), pairs.get(0));
        assertTrue(pairs.contains(new SeoPolicy.ComparisonPair("CVX", "XOM")));
        assertTrue(pairs.contains(new SeoPolicy.ComparisonPair("SPHD", "VYM")));
        assertFalse(pairs.contains(new SeoPolicy.ComparisonPair("KO", "SCHD")));
    }

    @Test
    void relatedComparisonsPrioritizeSharedTickerClusters() {
        List<SeoPolicy.ComparisonSpotlight> related = SeoPolicy.getRelatedComparisonSpotlights(
                "SCHD",
                List.of("AAPL", "MSFT", "CVX", "XOM", "DGRO", "HDV", "JEPI", "JEPQ", "JNJ", "KO",
                        "LMT", "O", "PEP", "PFE", "RTX", "SCHD", "SPHD", "VTI", "VYM"),
                4);

        assertEquals(4, related.size());
        assertTrue(related.stream().anyMatch(spotlight -> "JEPI vs SCHD".equals(spotlight.label())));
        assertTrue(related.stream().allMatch(spotlight -> spotlight.label().contains("SCHD")));
    }
}
