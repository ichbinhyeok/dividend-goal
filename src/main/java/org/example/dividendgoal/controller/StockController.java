package org.example.dividendgoal.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.dividendgoal.model.GeneratedContent;
import org.example.dividendgoal.model.LifestyleItem;
import org.example.dividendgoal.model.Stock;
import org.example.dividendgoal.service.*;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
            HttpServletResponse response, // [SEO] Freshness용
            Model model) {
        setCacheControl(response);
        double monthlyAmount = parseAmount(amountSegment);
        Stock stock = stockDataService.findByTicker(ticker)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticker not found"));

        boolean isDataAvailable = stock.getYield() > 0;
        model.addAttribute("isDataAvailable", isDataAvailable);

        double requiredInvestment = 0;
        double monthlyIncomeForTable = 0;
        GeneratedContent generatedContent;

        if (isDataAvailable) {
            // [LOGIC CHANGE] User target is NET (After-Tax).
            // "I want $1,000 in my pocket" means we need to target ~$1,182 Gross.
            requiredInvestment = dividendCalculationService.calculateRequiredInvestmentForNetIncome(monthlyAmount,
                    stock.getYield());

            // Re-calculate Gross Monthly Income from the new Capital
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

        addSeoFreshnessAttributes(model, pageTitle, pageDescription);

        // [SEO] Advanced Schema: Breadcrumb + Dataset + FAQ
        String schemaJson = generateSchemaJson(stock, monthlyAmount, requiredInvestment);
        model.addAttribute("jsonLdSchema", schemaJson);

        // [SEO] Duplicate Content Protection
        // Only index specific "Golden" amounts. User generated amounts (e.g. 543)
        // should be noindex.
        List<Double> goldenAmounts = List.of(500.0, 1000.0, 2000.0, 5000.0);
        boolean isGolden = goldenAmounts.contains(monthlyAmount);
        model.addAttribute("shouldIndex", isGolden);

        // [SEO] Internal Linking: Similar stocks from same sector
        List<Stock> similarStocks = stockDataService.getSimilarStocks(stock.getSector(), stock.getTicker(), 4);
        model.addAttribute("similarStocks", similarStocks);

        return "result";
    }

    @GetMapping("/how-much-income/{capital}/{ticker}")
    public String showIncomeIllustration(@PathVariable("capital") String capitalSegment,
            @PathVariable("ticker") String ticker,
            HttpServletRequest request, // [SEO] Canonical용
            HttpServletResponse response, // [SEO] Freshness용
            Model model) {
        setCacheControl(response);
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

        addSeoFreshnessAttributes(model, pageTitle, pageDescription);

        // [SEO] Advanced Schema: Breadcrumb + Dataset (Income Mode)
        // Reusing same generator but adapting for Income mode logic if needed,
        // for simplicity we map income to monthlyAmount param context or overload.
        String schemaJson = generateSchemaJson(stock, monthlyIncome, capital);
        model.addAttribute("jsonLdSchema", schemaJson);
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

    // [SEO] Freshness Automation Helper
    private void addSeoFreshnessAttributes(Model model, String baseTitle, String baseDescription) {
        LocalDate now = LocalDate.now();
        String monthYear = now.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        String refreshText = "Updated " + monthYear;

        model.addAttribute("currentYear", now.getYear());
        model.addAttribute("currentDate", now.format(DateTimeFormatter.ISO_LOCAL_DATE)); // 2026-01-15
        model.addAttribute("refreshText", refreshText);

        // Update Title and Description with Freshness info
        model.addAttribute("pageTitle", baseTitle + " (" + refreshText + ")");
        model.addAttribute("pageDescription", refreshText + " | " + baseDescription);
    }

    private void setCacheControl(HttpServletResponse response) {
        // [Performance] Dynamic calculation pages: 5-minute browser caching (balances
        // freshness & performance)
        // Changed from no-cache to allow browser caching for repeat visitors
        response.setHeader("Cache-Control", "public, max-age=300"); // 5 minutes

        // [SEO] 3-Month Autopilot Strategy: Last-Modified = 1st day of current month
        // This signals "Monthly Verified Content" without spamming daily updates.
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        long lastModTime = java.sql.Timestamp.valueOf(firstDayOfMonth.atStartOfDay()).getTime();
        response.setDateHeader("Last-Modified", lastModTime);
    }

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

    // [SEO] Advanced Schema Generator
    private String generateSchemaJson(Stock stock, double monthlyTarget, double capitalRequired) {
        String ticker = stock.getTicker();
        String name = stock.getName();
        String today = LocalDate.now().toString();

        return String.format(
                """
                        {
                          "@context": "https://schema.org",
                          "@graph": [
                            {
                              "@type": "BreadcrumbList",
                              "itemListElement": [
                                { "@type": "ListItem", "position": 1, "name": "Home", "item": "https://dividend-goal.com/" },
                                { "@type": "ListItem", "position": 2, "name": "Calculator", "item": "https://dividend-goal.com/how-much-dividend/" },
                                { "@type": "ListItem", "position": 3, "name": "%s Analysis", "item": "https://dividend-goal.com/how-much-dividend/1000-per-month/%s" }
                              ]
                            },
                            {
                              "@type": "Dataset",
                              "name": "%s Dividend Calculation Data",
                              "description": "Required capital analysis to generate $%.0f/month using %s (%s) stock dividends.",
                              "license": "https://dividend-goal.com/terms",
                              "creator": { "@type": "Organization", "name": "Dividend Goal" },
                              "dateModified": "%s"
                            },
                            {
                              "@type": "FAQPage",
                              "mainEntity": [
                                {
                                  "@type": "Question",
                                  "name": "How much %s do I need for $%.0f montly income?",
                                  "acceptedAnswer": {
                                    "@type": "Answer",
                                    "text": "Based on the current dividend yield of %.2f%%, you would need approximately $%s invested in %s."
                                  }
                                },
                                 {
                                  "@type": "Question",
                                  "name": "Is %s a good dividend stock?",
                                  "acceptedAnswer": {
                                    "@type": "Answer",
                                    "text": "%s pays a dividend yield of %.2f%%. It has a %d-year dividend growth streak, making it a viable candidate for income investors."
                                  }
                                }
                              ]
                            }
                          ]
                        }
                        """,
                ticker, ticker, // Breadcrumb
                name, monthlyTarget, name, ticker, today, // Dataset
                ticker, monthlyTarget, stock.getYield(), DOLLAR_FORMAT.format(capitalRequired), name, // FAQ Q1
                ticker, name, stock.getYield(), stock.getConsecutiveGrowthYears() // FAQ Q2
        );
    }
}