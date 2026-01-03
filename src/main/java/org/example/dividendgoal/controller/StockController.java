package org.example.dividendgoal.controller;

import org.example.dividendgoal.model.GeneratedContent;
import org.example.dividendgoal.model.Stock;
import org.example.dividendgoal.service.ContentGenerationService;
import org.example.dividendgoal.service.DividendCalculationService;
import org.example.dividendgoal.service.DripSimulationService;
import org.example.dividendgoal.service.StockDataService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Controller
public class StockController {

    private final StockDataService stockDataService;
    private final DividendCalculationService dividendCalculationService;
    private final ContentGenerationService contentGenerationService;
    private final DripSimulationService dripSimulationService;

    // 랜덤 객체 추가
    private final Random random = new Random();
    private static final DecimalFormat DOLLAR_FORMAT = new DecimalFormat("#,###.##");

    // 생성자에서 LifestyleMeaningService 제거 (이제 안 씀)
    public StockController(StockDataService stockDataService,
                           DividendCalculationService dividendCalculationService,
                           ContentGenerationService contentGenerationService,
                           DripSimulationService dripSimulationService) {
        this.stockDataService = stockDataService;
        this.dividendCalculationService = dividendCalculationService;
        this.contentGenerationService = contentGenerationService;
        this.dripSimulationService = dripSimulationService;
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

        // 타임머신 로직
        if (stock.getDividendGrowth() > 0) {
            List<Map<String, Object>> timeMachine = new ArrayList<>();
            int[] yearsToCheck = {1, 3, 5, 10};

            for (int year : yearsToCheck) {
                double futureCap = dividendCalculationService.calculateHypotheticalCapital(
                        monthlyAmount, stock.getYield(), stock.getDividendGrowth(), year
                );
                double savedMoney = requiredInvestment - futureCap;

                Map<String, Object> data = new HashMap<>();
                data.put("year", year);
                data.put("capital", futureCap);
                data.put("saved", savedMoney);
                timeMachine.add(data);
            }
            model.addAttribute("timeMachine", timeMachine);
        }
        model.addAttribute("dividendGrowth", stock.getDividendGrowth());

        model.addAttribute("calculationMode", "TARGET");
        model.addAttribute("monthlyAmount", monthlyAmount);
        model.addAttribute("annualAmount", monthlyAmount * 12);
        model.addAttribute("requiredInvestment", requiredInvestment);
        model.addAttribute("capitalInput", requiredInvestment);
        model.addAttribute("monthlyIncome", monthlyIncomeForTable);
        model.addAttribute("content", generatedContent);
        model.addAttribute("formattedRequiredInvestment", DOLLAR_FORMAT.format(requiredInvestment));
        model.addAttribute("dripProjections", dripSimulationService.simulate(requiredInvestment, stock.getYield()));

        // ▼▼▼ [수정] 서비스 대신 내부 메소드 호출 ▼▼▼
        model.addAttribute("lifestyleMeaning", getLifestyleComment(monthlyAmount));

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

        model.addAttribute("dividendGrowth", stock.getDividendGrowth());
        model.addAttribute("calculationMode", "INCOME");
        model.addAttribute("capitalInput", capital);
        model.addAttribute("monthlyIncome", monthlyIncome);
        model.addAttribute("annualAmount", annualIncome);
        model.addAttribute("monthlyAmount", monthlyIncome);
        model.addAttribute("content", generatedContent);
        model.addAttribute("dripProjections", dripSimulationService.simulate(capital, stock.getYield()));

        // ▼▼▼ [수정] 서비스 대신 내부 메소드 호출 ▼▼▼
        model.addAttribute("lifestyleMeaning", getLifestyleComment(monthlyIncome));

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

    // ▼▼▼ [신규] 미국 감성 랜덤 멘트 생성기 ▼▼▼
    private String getLifestyleComment(double amount) {
        List<String> options;

        if (amount < 50) {
            options = List.of(
                    "☕ Coffee is on the house! Enjoy your free Starbucks every week.",
                    "🎬 Netflix is free forever! Dividends cover your subscription.",
                    "🍕 Pizza night! Treat yourself to a free meal every month.",
                    "🎵 Your Spotify or Apple Music bill is now $0. Enjoy the tunes!"
            );
        } else if (amount < 300) {
            options = List.of(
                    "💡 Utilities paid! Keep the lights on without touching your paycheck.",
                    "🌐 High-speed Internet is free. Surf the web on your dividends.",
                    "⛽ Gas money sorted! Your commute just got a lot cheaper.",
                    "💪 Gym membership covered! Get fit while your money works out."
            );
        } else if (amount < 1000) {
            options = List.of(
                    "🛒 Free Groceries! Fill your cart without checking price tags.",
                    "🚗 Car payment? Gone. You're effectively driving for free.",
                    "✈️ Weekend getaway! You can fly somewhere nice every quarter.",
                    "🏥 Health Insurance premiums covered. Peace of mind secured."
            );
        } else if (amount < 3000) {
            options = List.of(
                    "🏠 Rent is FREE! Living cost-free is a superpower.",
                    "🧱 Mortgage crusher! Your house is basically paying for itself.",
                    "🎓 Student loans? Dividends are paying them off for you.",
                    "🏝️ You could live like a king in Bali or Thailand with this cash flow."
            );
        } else if (amount < 5000) {
            options = List.of(
                    "🚀 Financial Freedom approaching! You can quit the rat race.",
                    "👔 You are your own boss now. This replaces an average salary.",
                    "⏳ You just bought 'Time'. The 9-to-5 grind is now optional.",
                    "📉 Market crash? Who cares! The cash keeps flowing in."
            );
        } else {
            options = List.of(
                    "💎 Fat FIRE achieved! This is generational wealth territory.",
                    "🏦 You are a walking bank. A pure passive income machine.",
                    "🌍 Travel the world forever. You have won the capitalism game.",
                    "👑 Top 1% earner. Your money is working harder than you ever did."
            );
        }

        return options.get(random.nextInt(options.size()));
    }
}