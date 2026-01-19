package org.example.dividendgoal.service;

import org.springframework.stereotype.Service;

@Service
public class DividendCalculationService {

    // 1. Gross Target (세전 목표)
    public double calculateRequiredInvestment(double monthlyAmount, double dividendYieldPercentage) {
        if (dividendYieldPercentage <= 0) {
            return 0;
        }
        double annualAmount = monthlyAmount * 12;
        return annualAmount / (dividendYieldPercentage / 100);
    }

    // 2. Net Target (세후 목표) - [NEW]
    // 세후 100만원을 받으려면, 세전으로는 100 / (1 - 0.154) 만큼 필요함.
    public double calculateRequiredInvestmentForNetIncome(double netMonthlyAmount, double dividendYieldPercentage) {
        if (dividendYieldPercentage <= 0)
            return 0;

        // Step 1: 세전 목표액 역산 (Gross-up)
        double taxRate = 0.154; // 배당소득세 15.4%
        double grossMonthlyAmount = netMonthlyAmount / (1.0 - taxRate);

        return calculateRequiredInvestment(grossMonthlyAmount, dividendYieldPercentage);
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
    public double calculateHypotheticalCapital(double monthlyTarget, double currentYield, double growthRate,
            int years) {
        if (currentYield <= 0)
            return 0;

        // N년 뒤의 예상 배당률 (Yield on Cost)
        // 공식: 현재배당률 * (1 + 성장률)^년수
        double futureYield = currentYield * Math.pow(1 + (growthRate / 100.0), years);

        double annualTarget = monthlyTarget * 12;
        return annualTarget / (futureYield / 100.0);
    }

    /**
     * [Phase 4] Freedom Date Calculator (은퇴 시기 계산)
     * 목표 월 배당금(targetMonthlyIncome)에 도달하기까지 걸리는 시간을 계산.
     * 가정:
     * 1. 매월 monthlyContribution 만큼 추가 투자.
     * 2. 발생하는 배당금은 전액 재투자 (Monthly Compounding).
     * 3. 배당률(yield)은 일정하다고 가정 (보수적 접근).
     */
    public java.time.LocalDate calculateFreedomDate(double targetMonthlyIncome, double currentYield,
            double monthlyContribution) {
        if (targetMonthlyIncome <= 0 || currentYield <= 0) {
            return java.time.LocalDate.now();
        }

        double annualYieldRate = currentYield / 100.0;
        double monthlyYieldRate = annualYieldRate / 12.0;

        double currentMonthlyIncome = 0.0;
        double investedCapital = 0.0;
        java.time.LocalDate date = java.time.LocalDate.now();

        // 최대 100년 루프 제한 (무한 루프 방지)
        int maxMonths = 12 * 100;
        int months = 0;

        while (currentMonthlyIncome < targetMonthlyIncome) {
            // 1. 월 적립
            investedCapital += monthlyContribution;

            // 2. 배당금 발생 (이달의 자본 기준)
            double dividendThisMonth = investedCapital * monthlyYieldRate;

            // 3. 배당 재투자
            investedCapital += dividendThisMonth;

            // 4. 새로운 월 배당금 계산
            currentMonthlyIncome = investedCapital * monthlyYieldRate;

            date = date.plusMonths(1);
            months++;

            if (months >= maxMonths)
                break;
        }

        return date;
    }
}