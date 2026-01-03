package org.example.dividendgoal.service;
import org.springframework.stereotype.Service;

@Service
public class DividendCalculationService {

    public double calculateRequiredInvestment(double monthlyAmount, double dividendYieldPercentage) {
        if (dividendYieldPercentage <= 0) {
            return 0;
        }
        double annualAmount = monthlyAmount * 12;
        return annualAmount / (dividendYieldPercentage / 100);
    }
}