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
        assertTrue(SeoPolicy.isIndexableTargetPage("vti", 5000));

        assertFalse(SeoPolicy.isIndexableTargetPage("ABBV", 1000));
        assertFalse(SeoPolicy.isIndexableTargetPage("SCHD", 300));
        assertFalse(SeoPolicy.isIndexableTargetPage("SCHD", 1000.5));
    }

    @Test
    void incomePagesAreAlwaysNoindex() {
        assertFalse(SeoPolicy.isIndexableIncomePage("SCHD", 100000));
    }

    @Test
    void lifestylePagesMustMatchCuratedIntent() {
        assertTrue(SeoPolicy.isIndexableLifestylePage("netflix-premium", "SCHD"));

        assertFalse(SeoPolicy.isIndexableLifestylePage("spotify-family", "SCHD"));
        assertFalse(SeoPolicy.isIndexableLifestylePage("netflix-premium", "ABBV"));
    }

    @Test
    void comparisonPagesUseCanonicalAlphabeticalOrder() {
        SeoPolicy.ComparisonPair pair = SeoPolicy.canonicalComparisonPair("VYM", "SCHD");

        assertEquals("SCHD", pair.left());
        assertEquals("VYM", pair.right());
        assertTrue(SeoPolicy.isIndexableComparisonPage("VYM", "SCHD"));
        assertFalse(SeoPolicy.isIndexableComparisonPage("ABBV", "SCHD"));
    }

    @Test
    void comparisonSitemapOnlyContainsAvailableCuratedPairs() {
        List<SeoPolicy.ComparisonPair> pairs = SeoPolicy.getIndexableComparisonPairs(
                List.of("SCHD", "VYM", "JEPI", "O", "VTI", "QYLD", "ABBV"));

        assertEquals(15, pairs.size());
        assertEquals(new SeoPolicy.ComparisonPair("SCHD", "VYM"), pairs.get(0));
        assertTrue(pairs.contains(new SeoPolicy.ComparisonPair("JEPI", "SCHD")));
        assertTrue(pairs.contains(new SeoPolicy.ComparisonPair("QYLD", "VTI")));
    }
}
