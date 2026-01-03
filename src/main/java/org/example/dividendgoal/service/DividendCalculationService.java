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

    public double calculateMonthlyIncome(double capital, double dividendYieldPercentage) {
        if (dividendYieldPercentage <= 0 || capital <= 0) {
            return 0;
        }
        return capital * (dividendYieldPercentage / 100) / 12;
    }

    /**
     * [추가됨] 타임머신 계산기
     * 배당금이 매년 growthRate 만큼 성장한다고 가정할 때,
     * years 년 뒤에 monthlyTarget을 받으려면 '지금' 얼마를 투자해야 하는가?
     */
    public double calculateHypotheticalCapital(double monthlyTarget, double currentYield, double growthRate, int years) {
        if (currentYield <= 0) return 0;

        // N년 뒤의 예상 배당률 (Yield on Cost)
        // 공식: 현재배당률 * (1 + 성장률)^년수
        double futureYield = currentYield * Math.pow(1 + (growthRate / 100.0), years);

        double annualTarget = monthlyTarget * 12;
        return annualTarget / (futureYield / 100.0);
    }
}