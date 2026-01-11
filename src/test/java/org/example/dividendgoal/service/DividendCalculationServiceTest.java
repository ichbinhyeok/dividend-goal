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
}
