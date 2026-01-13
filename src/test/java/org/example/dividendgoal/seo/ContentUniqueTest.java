package org.example.dividendgoal.seo;

import org.example.dividendgoal.model.GeneratedContent;
import org.example.dividendgoal.model.Stock;
import org.example.dividendgoal.service.ContentGenerationService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContentUniqueTest {

    private final ContentGenerationService service = new ContentGenerationService();

    @Test
    void testSectorAnalysisLogic() {
        // 1. Setup AAPL (Low Yield vs Sector)
        Stock aapl = new Stock();
        aapl.setTicker("AAPL");
        aapl.setName("Apple Inc.");
        aapl.setSector("Technology");
        aapl.setYield(0.5);
        aapl.setSectorMedianYield(1.5); // Median is significantly higher -> AAPL is lower

        // 2. Setup SCHD (High Yield vs Sector)
        Stock schd = new Stock();
        schd.setTicker("SCHD");
        schd.setName("Schwab US Div");
        schd.setSector("ETF");
        schd.setYield(3.8);
        schd.setSectorMedianYield(2.5); // Median is lower -> SCHD is higher

        // 3. Generate Content
        GeneratedContent contentAapl = service.buildContent(aapl, 1000, 200000);
        GeneratedContent contentSchd = service.buildContent(schd, 1000, 20000);

        // 4. Assertions
        System.out.println("AAPL Analysis: " + contentAapl.getWhatIsTicker());
        System.out.println("SCHD Analysis: " + contentSchd.getWhatIsTicker());

        // Check Logic correctness
        assertTrue(contentAapl.getWhatIsTicker().contains("lower"),
                "AAPL analysis should mention yield is 'lower' than median");
        assertTrue(contentSchd.getWhatIsTicker().contains("higher"),
                "SCHD analysis should mention yield is 'higher' than median");

        // Check Uniqueness
        assertNotEquals(contentAapl.getWhatIsTicker(), contentSchd.getWhatIsTicker(),
                "Analysis text should be unique per stock");
    }

    @Test
    void testSafetyAnalysisLogic() {
        Stock risky = new Stock();
        risky.setTicker("RISK");
        risky.setPayoutRatio(120.0); // Dangerous
        risky.setConsecutiveGrowthYears(1);

        Stock safe = new Stock();
        safe.setTicker("SAFE");
        safe.setPayoutRatio(40.0); // Safe
        safe.setConsecutiveGrowthYears(25);

        GeneratedContent riskContent = service.buildContent(risky, 1000, 100);
        GeneratedContent safeContent = service.buildContent(safe, 1000, 100);

        System.out.println("Risky Analysis: " + riskContent.getInvestingAngle());
        System.out.println("Safe Analysis: " + safeContent.getInvestingAngle());

        assertTrue(riskContent.getInvestingAngle().contains("tight"), "High payout should range warning");
        assertTrue(safeContent.getInvestingAngle().contains("very safe"), "Low payout should be safe");
        assertTrue(safeContent.getInvestingAngle().contains("Dividend Aristocrat"), "25 years should be Aristocrat");
    }
}
