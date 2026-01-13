package org.example.dividendgoal.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.dividendgoal.model.GeneratedContent;
import org.example.dividendgoal.model.LifestyleItem;
import org.example.dividendgoal.model.Stock;
import org.example.dividendgoal.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
    private final LifestyleService lifestyleService; // [NEW] 주입

    private final Random random = new Random();
    private static final DecimalFormat DOLLAR_FORMAT = new DecimalFormat("#,###.##");

    public StockController(StockDataService stockDataService,
            DividendCalculationService dividendCalculationService,
            ContentGenerationService contentGenerationService,
            DripSimulationService dripSimulationService,
            LifestyleService lifestyleService) {
        this.stockDataService = stockDataService;
        this.dividendCalculationService = dividendCalculationService;
        this.contentGenerationService = contentGenerationService;
        this.dripSimulationService = dripSimulationService;
        this.lifestyleService = lifestyleService;
    }

    @GetMapping("/how-much-dividend/{amount}-per-month/{ticker}")
    public String showDividendPlan(@PathVariable("amount") String amountSegment,
            @PathVariable("ticker") String ticker,
            HttpServletRequest request, // [SEO] Canonical용
            Model model) {
        double monthlyAmount = parseAmount(amountSegment);
        Stock stock = stockDataService.findByTicker(ticker)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticker not found"));

        boolean isDataAvailable = stock.getYield() > 0;
        model.addAttribute("isDataAvailable", isDataAvailable);

        double requiredInvestment = 0;
        double monthlyIncomeForTable = 0;
        GeneratedContent generatedContent;

        if (isDataAvailable) {
            requiredInvestment = dividendCalculationService.calculateRequiredInvestment(monthlyAmount,
                    stock.getYield());
            monthlyIncomeForTable = dividendCalculationService.calculateMonthlyIncome(requiredInvestment,
                    stock.getYield());
            generatedContent = contentGenerationService.buildContent(stock, monthlyAmount, requiredInvestment);
        } else {
            // [YMYL] Missing Data Fallback
            generatedContent = new GeneratedContent(
                    "Data Unavailable: Dividend yield data is missing for this ticker.",
                    "Data Unavailable",
                    "Data Unavailable",
                    "Data Unavailable",
                    "Disclaimer: Data is missing.");
        }

        addSharedAttributes(model, stock);

        // --- 타임머신 로직 ---
        if (isDataAvailable && stock.getDividendGrowth() > 0) {
            List<Map<String, Object>> timeMachine = new ArrayList<>();
            int[] yearsToCheck = { 1, 3, 5, 10 };

            for (int year : yearsToCheck) {
                double futureCap = dividendCalculationService.calculateHypotheticalCapital(
                        monthlyAmount, stock.getYield(), stock.getDividendGrowth(), year);
                double savedMoney = requiredInvestment - futureCap;

                Map<String, Object> data = new HashMap<>();
                data.put("year", year);
                data.put("capital", futureCap);
                data.put("saved", savedMoney);
                timeMachine.add(data);
            }
            model.addAttribute("timeMachine", timeMachine);
        }

        // --- [NEW] 내부 링크 (Internal Linking) ---
        // 하단 박스에 "이 주식으로 넷플릭스도 공짜?" 제안용 랜덤 아이템
        if (isDataAvailable) {
            LifestyleItem recommendedItem = lifestyleService.getRandomItem();
            model.addAttribute("recommendedItem", recommendedItem);
        }

        // --- [SEO] Canonical URL ---
        String currentUrl = ServletUriComponentsBuilder.fromRequestUri(request).build().toUriString();
        model.addAttribute("currentUrl", currentUrl);

        model.addAttribute("dividendGrowth", stock.getDividendGrowth());
        model.addAttribute("calculationMode", "TARGET");
        model.addAttribute("monthlyAmount", monthlyAmount);
        model.addAttribute("annualAmount", monthlyAmount * 12);
        model.addAttribute("requiredInvestment", requiredInvestment);
        model.addAttribute("formattedRequiredInvestment", DOLLAR_FORMAT.format(requiredInvestment));
        model.addAttribute("monthlyIncome", monthlyIncomeForTable);
        model.addAttribute("content", generatedContent);

        if (isDataAvailable) {
            model.addAttribute("dripProjections", dripSimulationService.simulate(requiredInvestment, stock.getYield()));
            // 멘트 생성기 호출
            model.addAttribute("lifestyleMeaning", getLifestyleComment(monthlyAmount));
        } else {
            model.addAttribute("lifestyleMeaning", "Calculation unavailable due to missing data.");
        }

        String pageTitle = String.format("How much %s to get $%.0f/month? | Money First", stock.getTicker(),
                monthlyAmount);
        String pageDescription = String.format(
                "Calculation: You need $%s in %s (%s) to target $%.0f monthly dividends with a %.2f%% yield.",
                DOLLAR_FORMAT.format(requiredInvestment), stock.getName(), stock.getTicker(), monthlyAmount,
                stock.getYield());

        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("pageDescription", pageDescription);

        // [SEO] Duplicate Content Protection
        // Only index specific "Golden" amounts. User generated amounts (e.g. 543)
        // should be noindex.
        List<Double> goldenAmounts = List.of(500.0, 1000.0, 2000.0, 5000.0);
        boolean isGolden = goldenAmounts.contains(monthlyAmount);
        model.addAttribute("shouldIndex", isGolden);

        return "result";
    }

    @GetMapping("/how-much-income/{capital}/{ticker}")
    public String showIncomeIllustration(@PathVariable("capital") String capitalSegment,
            @PathVariable("ticker") String ticker,
            HttpServletRequest request, // [SEO] Canonical용
            Model model) {
        double capital = parseAmount(capitalSegment);
        Stock stock = stockDataService.findByTicker(ticker)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticker not found"));

        boolean isDataAvailable = stock.getYield() > 0;
        model.addAttribute("isDataAvailable", isDataAvailable);

        double monthlyIncome = 0;
        double annualIncome = 0;
        GeneratedContent generatedContent;

        if (isDataAvailable) {
            monthlyIncome = dividendCalculationService.calculateMonthlyIncome(capital, stock.getYield());
            annualIncome = monthlyIncome * 12;
            generatedContent = contentGenerationService.buildIncomeContent(stock, capital, monthlyIncome);
        } else {
            generatedContent = new GeneratedContent(
                    "Data Unavailable", "Data Unavailable", "Data Unavailable", "Data Unavailable", "Disclaimer");
        }

        addSharedAttributes(model, stock);

        // --- [NEW] 내부 링크 (Internal Linking) ---
        if (isDataAvailable) {
            LifestyleItem recommendedItem = lifestyleService.getRandomItem();
            model.addAttribute("recommendedItem", recommendedItem);
        }

        // --- [SEO] Canonical URL ---
        String currentUrl = ServletUriComponentsBuilder.fromRequestUri(request).build().toUriString();
        model.addAttribute("currentUrl", currentUrl);

        model.addAttribute("dividendGrowth", stock.getDividendGrowth());
        model.addAttribute("calculationMode", "INCOME");
        model.addAttribute("capitalInput", capital);
        model.addAttribute("monthlyIncome", monthlyIncome);
        model.addAttribute("annualAmount", annualIncome);
        model.addAttribute("monthlyAmount", monthlyIncome);
        model.addAttribute("content", generatedContent);

        if (isDataAvailable) {
            model.addAttribute("dripProjections", dripSimulationService.simulate(capital, stock.getYield()));
            // 멘트 생성기 호출
            model.addAttribute("lifestyleMeaning", getLifestyleComment(monthlyIncome));
        } else {
            model.addAttribute("lifestyleMeaning", "Calculation unavailable due to missing data.");
        }
        model.addAttribute("formattedRequiredInvestment", DOLLAR_FORMAT.format(capital));

        String pageTitle = String.format("How much monthly income from $%,.0f in %s? | Money First", capital,
                stock.getTicker());
        String pageDescription = String.format(
                "Illustrate estimated monthly dividends from $%,.0f in %s (%s) using a %.2f%% trailing yield.",
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

    // [신규] 미국 감성 랜덤 멘트 생성기
    private String getLifestyleComment(double amount) {
        List<String> options;

        if (amount < 50) {
            options = List.of(
                    "☕ Coffee is on the house! Enjoy your free Starbucks every week.",
                    "🎬 Netflix is free forever! Dividends cover your subscription.",
                    "🍕 Pizza night! Treat yourself to a free meal every month.",
                    "🎵 Your Spotify or Apple Music bill is now $0. Enjoy the tunes!");
        } else if (amount < 300) {
            options = List.of(
                    "💡 Utilities paid! Keep the lights on without touching your paycheck.",
                    "🌐 High-speed Internet is free. Surf the web on your dividends.",
                    "⛽ Gas money sorted! Your commute just got a lot cheaper.",
                    "💪 Gym membership covered! Get fit while your money works out.");
        } else if (amount < 1000) {
            options = List.of(
                    "🛒 Free Groceries! Fill your cart without checking price tags.",
                    "🚗 Car payment? Gone. You're effectively driving for free.",
                    "✈️ Weekend getaway! You can fly somewhere nice every quarter.",
                    "🏥 Health Insurance premiums covered. Peace of mind secured.");
        } else if (amount < 3000) {
            options = List.of(
                    "🏠 Rent is FREE! Living cost-free is a superpower.",
                    "🧱 Mortgage crusher! Your house is basically paying for itself.",
                    "🎓 Student loans? Dividends are paying them off for you.",
                    "🏝️ You could live like a king in Bali or Thailand with this cash flow.");
        } else if (amount < 5000) {
            options = List.of(
                    "🚀 Financial Freedom approaching! You can quit the rat race.",
                    "👔 You are your own boss now. This replaces an average salary.",
                    "⏳ You just bought 'Time'. The 9-to-5 grind is now optional.",
                    "📉 Market crash? Who cares! The cash keeps flowing in.");
        } else {
            options = List.of(
                    "💎 Fat FIRE achieved! This is generational wealth territory.",
                    "🏦 You are a walking bank. A pure passive income machine.",
                    "🌍 Travel the world forever. You have won the capitalism game.",
                    "👑 Top 1% earner. Your money is working harder than you ever did.");
        }

        return options.get(random.nextInt(options.size()));
    }
}