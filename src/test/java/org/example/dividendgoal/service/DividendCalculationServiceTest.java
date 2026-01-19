package org.example.dividendgoal.service;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DividendCalculationServiceTest {

    private final DividendCalculationService service = new DividendCalculationService();

    @Test
    public void testFreedomDate_Basic() {
        // Goal: $100/mo dividend
        // Yield: 12% (1% per month roughly)
        // Contribution: $1000/mo

        // Rough math:
        // Need $10,000 capital for $100/mo at 12% yield.
        // Saving $1,000/mo -> roughly 10 months without interest.
        // With compounding, should be slightly less than 10 months.

        double targetIncome = 100.0;
        double yield = 12.0;
        double contribution = 1000.0;

        LocalDate today = LocalDate.now();
        LocalDate result = service.calculateFreedomDate(targetIncome, yield, contribution);

        long months = ChronoUnit.MONTHS.between(today, result);

        System.out.println("Months to Freedom: " + months);

        // Expect roughly 9-10 months.
        assertTrue(months >= 9 && months <= 10, "Should take about 9-10 months");
    }

    @Test
    public void testFreedomDate_Impossible() {
        // Goal: $1,000,000/mo
        // Savings: $1/mo
        // Should hit max cap (100 years)
        LocalDate today = LocalDate.now();
        LocalDate result = service.calculateFreedomDate(1_000_000, 5.0, 1.0);

        long years = ChronoUnit.YEARS.between(today, result);
        assertTrue(years >= 99, "Should hit the 100 year safety limit");
    }

    @Test
    public void testNetIncomeCalculation() {
        // Goal: $1,000 Net Income
        // Tax: 15.4%
        // Target Gross should be: 1000 / (1 - 0.154) = 1182.03...

        // Yield: 5%
        // Required Capital = Annual Gross / 0.05
        // Annual Gross = 1182.03 * 12 = 14,184.39...
        // Capital = 14,184.39 / 0.05 = 283,687.94...

        double netTarget = 1000;
        double yield = 5.0;

        double requiredCapital = service.calculateRequiredInvestmentForNetIncome(netTarget, yield);

        // Manual calc for verification
        double expectedGrossMonthly = 1000 / (1.0 - 0.154);
        double expectedAnnual = expectedGrossMonthly * 12;
        double expectedCapital = expectedAnnual / 0.05;

        assertEquals(expectedCapital, requiredCapital, 0.01, "Capital calculation mismatch");

        // Additional Check: If we have this capital, does it yield the NET amount?
        double grossIncome = service.calculateMonthlyIncome(requiredCapital, yield);
        double netIncome = grossIncome * (1 - 0.154);

        assertEquals(1000.0, netIncome, 0.01, "Reverse check failed: Net income should be exactly 1000");
    }
}
