package org.example.dividendgoal.controller;


import org.example.dividendgoal.model.GeneratedContent;
import org.example.dividendgoal.model.Stock;
import org.example.dividendgoal.service.ContentGenerationService;
import org.example.dividendgoal.service.DividendCalculationService;
import org.example.dividendgoal.service.StockDataService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.text.DecimalFormat;

@Controller
public class StockController {

    private final StockDataService stockDataService;
    private final DividendCalculationService dividendCalculationService;
    private final ContentGenerationService contentGenerationService;
    private static final DecimalFormat DOLLAR_FORMAT = new DecimalFormat("#,###.##");

    public StockController(StockDataService stockDataService,
                           DividendCalculationService dividendCalculationService,
                           ContentGenerationService contentGenerationService) {
        this.stockDataService = stockDataService;
        this.dividendCalculationService = dividendCalculationService;
        this.contentGenerationService = contentGenerationService;
    }

    @GetMapping("/how-much-dividend/{amount}-per-month/{ticker}")
    public String showDividendPlan(@PathVariable("amount") String amountSegment,
                                   @PathVariable("ticker") String ticker,
                                   Model model) {
        double monthlyAmount = parseAmount(amountSegment);
        Stock stock = stockDataService.findByTicker(ticker)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticker not found"));

        double requiredInvestment = dividendCalculationService.calculateRequiredInvestment(monthlyAmount, stock.getYield());
        GeneratedContent generatedContent = contentGenerationService.buildContent(stock, monthlyAmount, requiredInvestment);

        model.addAttribute("stock", stock);
        model.addAttribute("monthlyAmount", monthlyAmount);
        model.addAttribute("annualAmount", monthlyAmount * 12);
        model.addAttribute("requiredInvestment", requiredInvestment);
        model.addAttribute("content", generatedContent);
        model.addAttribute("formattedRequiredInvestment", DOLLAR_FORMAT.format(requiredInvestment));
        model.addAttribute("stocks", stockDataService.getAllStocks());

        String pageTitle = String.format("How much capital is required for %s to target $%.0f/month? | Money First", stock.getTicker(), monthlyAmount);
        String pageDescription = String.format("Calculate how much money is needed in %s (%s) to illustrate a $%.0f monthly dividend target. With a %.2f%% yield, the estimated required capital is $%,.0f.",
                stock.getName(), stock.getTicker(), monthlyAmount, stock.getYield(), requiredInvestment);

        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("pageDescription", pageDescription);
        return "result";
    }

    private double parseAmount(String segment) {
        try {
            String sanitized = segment.replaceAll("[^0-9.]", "");
            double value = Double.parseDouble(sanitized);
            if (value <= 0) {
                throw new NumberFormatException("Amount must be positive");
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid amount");
        }
    }
}