package org.example.dividendgoal.service;

import org.example.dividendgoal.model.DripProjection;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DripSimulationService {

    private static final List<Integer> YEARS = List.of(1, 3, 5, 10);

    public List<DripProjection> simulate(double startingCapital, double dividendYieldPercentage) {
        List<DripProjection> projections = new ArrayList<>();
        double annualRate = dividendYieldPercentage / 100.0;

        for (Integer year : YEARS) {
            double estimatedCapital = startingCapital * Math.pow(1 + annualRate, year);
            double estimatedMonthlyIncome = estimatedCapital * annualRate / 12;
            projections.add(new DripProjection(year, estimatedCapital, estimatedMonthlyIncome));
        }

        return projections;
    }
}
