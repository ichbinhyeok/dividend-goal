package org.example.dividendgoal.service;

import org.example.dividendgoal.model.GeneratedContent;
import org.example.dividendgoal.model.Stock;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class ContentGenerationService {

    public GeneratedContent buildContent(Stock stock, double monthlyAmount, double requiredInvestment) {
        // [YMYL] Data-Driven Analysis (Safe Fallback)
        if (stock.getYield() <= 0) {
            return new GeneratedContent(
                    "Data Unavailable: Dividend yield data is missing for this ticker.",
                    "Data Unavailable",
                    "Data Unavailable",
                    "Data Unavailable",
                    getDisclaimer());
        }

        String intro = generateIntro(stock, monthlyAmount, requiredInvestment);
        String sectorAnalysis = generateSectorAnalysis(stock);
        String lifestyle = generateMeaning(monthlyAmount);
        String safetyAnalysis = generateSafetyAnalysis(stock);
        String disclaimer = getDisclaimer();

        return new GeneratedContent(intro, sectorAnalysis, lifestyle, safetyAnalysis, disclaimer);
    }

    // 기존 메서드 호환성 유지
    public GeneratedContent buildIncomeContent(Stock stock, double capital, double monthlyIncome) {
        return buildContent(stock, monthlyIncome, capital);
    }

    private String generateIntro(Stock stock, double monthlyAmount, double requiredInvestment) {
        StringBuilder intro = new StringBuilder();
        intro.append(String.format(
                "To generate a <strong>$%,.0f monthly dividend income</strong> from %s (%s), you would need an estimated investment of <strong>$%,.0f</strong> today, based on its current dividend yield of %.2f%%. ",
                monthlyAmount, stock.getName(), stock.getTicker(), requiredInvestment, stock.getYield()));

        // Add context about dividend investing
        intro.append("This calculation assumes the dividend rate remains constant over time. ");
        intro.append(String.format(
                "With %s's current yield, each $1,000 invested would generate approximately $%.2f in annual dividend income. ",
                stock.getTicker(), stock.getYield() * 10));

        // Add perspective on capital requirements
        if (requiredInvestment > 500000) {
            intro.append(
                    "This is a substantial capital requirement, typically pursued by high-net-worth individuals or institutional investors focusing on generational wealth building.");
        } else if (requiredInvestment > 100000) {
            intro.append(
                    "This level of capital is commonly targeted by mid-career professionals building towards financial independence or early retirement.");
        } else {
            intro.append(
                    "This is an accessible entry point for many investors looking to supplement their income with passive cash flow.");
        }

        return intro.toString();
    }

    private String generateSectorAnalysis(Stock stock) {
        double sectorMedian = stock.getSectorMedianYield();
        double myYield = stock.getYield();

        if (sectorMedian <= 0) {
            return "Sector interaction data is currently unavailable for this ticker.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s operates in the <strong>%s</strong> sector. ", stock.getName(), stock.getSector()));

        double diff = myYield - sectorMedian;
        double percentDiff = (diff / sectorMedian) * 100;

        if (diff > 1.0) {
            sb.append(String.format(
                    "Its yield of %.2f%% is significantly <strong>higher</strong> than the sector median of %.2f%% (approximately %.0f%% above average). ",
                    myYield, sectorMedian, Math.abs(percentDiff)));
            sb.append(
                    "This elevated yield often indicates one of three scenarios: the market may be pricing in higher risk due to business headwinds, the company may have a mature cash flow profile with limited growth prospects, or it could represent a genuine value opportunity overlooked by the broader market. ");
            sb.append(
                    "Investors should carefully examine the payout ratio and dividend history before committing capital.");
        } else if (diff < -1.0) {
            sb.append(String.format(
                    "Its yield of %.2f%% is <strong>lower</strong> than the sector median of %.2f%% (approximately %.0f%% below average). ",
                    myYield, sectorMedian, Math.abs(percentDiff)));
            sb.append(
                    "This typically suggests investors are pricing in higher growth expectations rather than immediate income. ");
            sb.append(
                    "Companies with lower yields often reinvest more capital into expansion, which may result in dividend growth over time but provides less immediate cash flow.");
        } else {
            sb.append(String.format(
                    "Its yield of %.2f%% is in line with the sector median of %.2f%%, suggesting it is fairly valued relative to its peers. ",
                    myYield, sectorMedian));
            sb.append(
                    "This equilibrium positioning indicates the stock follows typical sector patterns, neither commanding a growth premium nor trading at a distressed valuation.");
        }
        return sb.toString();
    }

    private String generateMeaning(double amount) {
        if (amount < 200)
            return "This amount covers basic subscriptions like Netflix, Spotify, or a weekly coffee run.";
        if (amount < 800)
            return "This level of passive income could offset major utility bills, car insurance, or a significant portion of groceries.";
        if (amount < 2500)
            return "Generating this monthly amount is comparable to having a rental property without the headaches of tenants or maintenance.";
        return "At this level, you are replacing a full-time median salary with purely passive income, achieving true financial independence.";
    }

    private String generateSafetyAnalysis(Stock stock) {
        StringBuilder sb = new StringBuilder();

        // Payout Ratio Analysis
        double payout = stock.getPayoutRatio();
        if (payout > 0) {
            sb.append(String.format("<strong>Payout Ratio Health:</strong> With a payout ratio of %.1f%%, ", payout));
            if (payout < 60) {
                sb.append(
                        "the dividend appears very safe, leaving ample retained earnings for reinvestment and growth. ");
            } else if (payout < 95) {
                sb.append(
                        "the company pays out a significant portion of earnings but maintains a sustainable balance. ");
            } else {
                sb.append("the dividend coverage is tight, which could signal future risk if earnings dip. ");
            }
        } else {
            sb.append("<strong>Payout Ratio:</strong> Data unavailable. ");
        }

        // Dividend Streak Analysis
        int years = stock.getConsecutiveGrowthYears();
        if (years > 0) {
            sb.append(String.format(
                    "<br><strong>Growth Streak:</strong> %s has raised its dividend for %d consecutive years, ",
                    stock.getTicker(), years));
            if (years > 24) {
                sb.append("classifying it as a reliable 'Dividend Aristocrat' candidate (or equivalent status).");
            } else if (years > 9) {
                sb.append("demonstrating specific commitment to shareholder returns over the last decade.");
            } else {
                sb.append("showing a starting track record of dividend growth.");
            }
        }

        return sb.toString();
    }

    private String getDisclaimer() {
        return "Disclaimer: This analysis is based on historical data and does not constitute financial advice. Dividend yields and payout ratios can change rapidly.";
    }

}
