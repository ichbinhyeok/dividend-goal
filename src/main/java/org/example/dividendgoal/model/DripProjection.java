package org.example.dividendgoal.model;

public class DripProjection {
    private final int year;
    private final double estimatedCapital;
    private final double estimatedMonthlyIncome;

    public DripProjection(int year, double estimatedCapital, double estimatedMonthlyIncome) {
        this.year = year;
        this.estimatedCapital = estimatedCapital;
        this.estimatedMonthlyIncome = estimatedMonthlyIncome;
    }

    public int getYear() {
        return year;
    }

    public double getEstimatedCapital() {
        return estimatedCapital;
    }

    public double getEstimatedMonthlyIncome() {
        return estimatedMonthlyIncome;
    }
}
