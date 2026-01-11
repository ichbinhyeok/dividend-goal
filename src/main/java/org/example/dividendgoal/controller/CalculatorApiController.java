package org.example.dividendgoal.controller;

import org.example.dividendgoal.service.DividendCalculationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/calculator")
public class CalculatorApiController {

    private final DividendCalculationService calculationService;

    public CalculatorApiController(DividendCalculationService calculationService) {
        this.calculationService = calculationService;
    }

    @PostMapping("/freedom-date")
    public ResponseEntity<Map<String, Object>> calculateFreedomDate(@RequestBody Map<String, Double> payload) {
        double targetIncome = payload.getOrDefault("targetIncome", 0.0);
        double yield = payload.getOrDefault("yield", 0.0);
        double monthlyContribution = payload.getOrDefault("monthlyContribution", 0.0);

        LocalDate freedomDate = calculationService.calculateFreedomDate(targetIncome, yield, monthlyContribution);
        LocalDate today = LocalDate.now();

        Period period = Period.between(today, freedomDate);

        Map<String, Object> response = new HashMap<>();
        response.put("freedomDate", freedomDate.toString()); // YYYY-MM-DD
        response.put("years", period.getYears());
        response.put("months", period.getMonths());
        response.put("formattedDate", freedomDate.getMonth().name() + " " + freedomDate.getYear()); // "MAY 2028"

        return ResponseEntity.ok(response);
    }
}
