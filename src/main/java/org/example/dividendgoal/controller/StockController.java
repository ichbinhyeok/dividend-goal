package org.example.dividendgoal.controller;


import org.example.dividendgoal.model.GeneratedContent;
import org.example.dividendgoal.model.Stock;
import org.example.dividendgoal.service.ContentGenerationService;
import org.example.dividendgoal.service.DividendCalculationService;
import org.example.dividendgoal.service.DripSimulationService;
import org.example.dividendgoal.service.LifestyleMeaningService;
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
    private final DripSimulationService dripSimulationService;
    private final LifestyleMeaningService lifestyleMeaningService;
    private static final DecimalFormat DOLLAR_FORMAT = new DecimalFormat("#,###.##");

    public StockController(StockDataService stockDataService,
                           DividendCalculationService dividendCalculationService,
                           ContentGenerationService contentGenerationService,
                           DripSimulationService dripSimulationService,
                           LifestyleMeaningService lifestyleMeaningService) {
        this.stockDataService = stockDataService;
        this.dividendCalculationService = dividendCalculationService;
        this.contentGenerationService = contentGenerationService;
        this.dripSimulationService = dripSimulationService;
        this.lifestyleMeaningService = lifestyleMeaningService;
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
        double monthlyIncomeForTable = dividendCalculationService.calculateMonthlyIncome(requiredInvestment, stock.getYield());
        addSharedAttributes(model, stock);

        model.addAttribute("calculationMode", "TARGET");
        model.addAttribute("monthlyAmount", monthlyAmount);
        model.addAttribute("annualAmount", monthlyAmount * 12);
        model.addAttribute("requiredInvestment", requiredInvestment);
        model.addAttribute("capitalInput", requiredInvestment);
        model.addAttribute("monthlyIncome", monthlyIncomeForTable);
        model.addAttribute("content", generatedContent);
        model.addAttribute("formattedRequiredInvestment", DOLLAR_FORMAT.format(requiredInvestment));
        model.addAttribute("dripProjections", dripSimulationService.simulate(requiredInvestment, stock.getYield()));
        model.addAttribute("lifestyleMeaning", lifestyleMeaningService.describe(monthlyAmount));

        String pageTitle = String.format("How much capital is required for %s to target $%.0f/month? | Money First", stock.getTicker(), monthlyAmount);
        String pageDescription = String.format("Calculate how much money is needed in %s (%s) to illustrate a $%.0f monthly dividend target. With a %.2f%% yield, the estimated required capital is $%,.0f.",
                stock.getName(), stock.getTicker(), monthlyAmount, stock.getYield(), requiredInvestment);

        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("pageDescription", pageDescription);
        return "result";
    }

    @GetMapping("/how-much-income/{capital}/{ticker}")
    public String showIncomeIllustration(@PathVariable("capital") String capitalSegment,
                                         @PathVariable("ticker") String ticker,
                                         Model model) {
        double capital = parseAmount(capitalSegment);
        Stock stock = stockDataService.findByTicker(ticker)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticker not found"));

        double monthlyIncome = dividendCalculationService.calculateMonthlyIncome(capital, stock.getYield());
        double annualIncome = monthlyIncome * 12;
        GeneratedContent generatedContent = contentGenerationService.buildIncomeContent(stock, capital, monthlyIncome);
        addSharedAttributes(model, stock);

        model.addAttribute("calculationMode", "INCOME");
        model.addAttribute("capitalInput", capital);
        model.addAttribute("monthlyIncome", monthlyIncome);
        model.addAttribute("annualAmount", annualIncome);
        model.addAttribute("monthlyAmount", monthlyIncome);
        model.addAttribute("content", generatedContent);
        model.addAttribute("dripProjections", dripSimulationService.simulate(capital, stock.getYield()));
        model.addAttribute("lifestyleMeaning", lifestyleMeaningService.describe(monthlyIncome));
        model.addAttribute("formattedRequiredInvestment", DOLLAR_FORMAT.format(capital));

        String pageTitle = String.format("How much monthly income from $%,.0f in %s? | Money First", capital, stock.getTicker());
        String pageDescription = String.format("Illustrate estimated monthly dividends from $%,.0f in %s (%s) using a %.2f%% trailing yield. Educational, stateless, and server-rendered for clarity.",
                capital, stock.getName(), stock.getTicker(), stock.getYield());

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

    private void addSharedAttributes(Model model, Stock stock) {
        model.addAttribute("stock", stock);
        model.addAttribute("stocks", stockDataService.getAllStocks());
    }
}
