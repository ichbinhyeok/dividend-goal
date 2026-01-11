package org.example.dividendgoal.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.dividendgoal.model.LifestyleItem;
import org.example.dividendgoal.model.Stock;
import org.example.dividendgoal.service.BestStockService;
import org.example.dividendgoal.service.DividendCalculationService;
import org.example.dividendgoal.service.LifestyleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class LifestyleGoalController {

    private final LifestyleService lifestyleService;
    private final BestStockService bestStockService;
    private final DividendCalculationService calculationService;

    public LifestyleGoalController(LifestyleService lifestyleService, BestStockService bestStockService,
            DividendCalculationService calculationService) {
        this.lifestyleService = lifestyleService;
        this.bestStockService = bestStockService;
        this.calculationService = calculationService;
    }

    // New Hub Page: /lifestyle/pay-for-netflix (No ticker in URL)
    @GetMapping("/lifestyle/pay-for-{itemSlug}")
    public String showGoalHub(@PathVariable String itemSlug, HttpServletRequest request, Model model) {

        // 1. Get Goal Item
        LifestyleItem item = lifestyleService.findBySlug(itemSlug)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found: " + itemSlug));

        // 2. Get Recommendations (The "Expert" Choice)
        List<Stock> safeStocks = bestStockService.getTopSafetyStocks(3);
        List<Stock> incomeStocks = bestStockService.getTopIncomeStocks(3);

        // 3. Prepare Comparison Data (Pre-calculate everything)
        List<Map<String, Object>> comparisonTable = safeStocks.stream().map(stock -> {
            Map<String, Object> map = new HashMap<>();
            double reqInfo = calculationService.calculateRequiredInvestment(item.getCost(), stock.getYield());
            map.put("ticker", stock.getTicker());
            map.put("name", stock.getName());
            map.put("yield", stock.getYield());
            map.put("risk", stock.getRisk());
            map.put("requiredCapital", reqInfo);
            map.put("formattedCapital", String.format("%,.0f", reqInfo));
            map.put("strategy", "Growth & Safety");
            return map;
        }).collect(Collectors.toList());

        // Add Income stocks to table
        comparisonTable.addAll(incomeStocks.stream().map(stock -> {
            Map<String, Object> map = new HashMap<>();
            double reqInfo = calculationService.calculateRequiredInvestment(item.getCost(), stock.getYield());
            map.put("ticker", stock.getTicker());
            map.put("name", stock.getName());
            map.put("yield", stock.getYield());
            map.put("risk", stock.getRisk());
            map.put("requiredCapital", reqInfo);
            map.put("formattedCapital", String.format("%,.0f", reqInfo));
            map.put("strategy", "High Income");
            return map;
        }).collect(Collectors.toList()));

        // 4. E-E-A-T Injection
        model.addAttribute("item", item);
        model.addAttribute("comparisonTable", comparisonTable);
        model.addAttribute("pageTitle", "Best Dividend Stocks to Pay for " + item.getName() + " (2026 Guide)");
        model.addAttribute("pageDescription",
                "Don't pay for " + item.getName()
                        + "! Compare Top 5 Dividend Stocks (JEPI, SCHD, etc.) that can cover your $" + item.getCost()
                        + " monthly bill forever.");
        model.addAttribute("canonicalUrl", ServletUriComponentsBuilder.fromRequestUri(request).build().toUriString());

        // Trust Signals
        model.addAttribute("dataSource", "Data derived from Seeking Alpha & Yahoo Finance API.");
        model.addAttribute("methodologyLink", "/about/methodology"); // We need to create this later

        return "lifestyle-hub"; // New template
    }
}
