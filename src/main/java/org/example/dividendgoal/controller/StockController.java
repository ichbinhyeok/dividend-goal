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

        // [SEO Update] Curiosity Gap Strategy (No Spoilers)
        String pageTitle = generateTargetModeTitle(stock.getTicker(), monthlyAmount);
        String pageDescription = generateTargetModeDesc(stock.getTicker(), monthlyAmount);
        String h1Text = String.format("Can %s Really Pay You $%s/Month?", stock.getTicker(),
                DOLLAR_FORMAT.format(monthlyAmount));

        model.addAttribute("pageHeading", h1Text);

        addSeoFreshnessAttributes(model, pageTitle, pageDescription);

        // [SEO] Advanced Schema: Breadcrumb + Dataset (FAQ removed from here, handled
        // separately)
        String schemaJson = generateSchemaJson(stock, monthlyAmount, requiredInvestment);
        model.addAttribute("jsonLdSchema", schemaJson);

        // [SEO] FAQ Schema (Standalone, Spoiler-Free)
        String faqJson = generateFaqSchema(stock.getTicker(), "monthly income", monthlyAmount);
        model.addAttribute("faqJson", faqJson);

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

        // [SEO Update] Curiosity Gap Strategy (No Spoilers)
        String pageTitle = generateIncomeModeTitle(stock.getTicker(), capital);
        String pageDescription = generateIncomeModeDesc(stock.getTicker(), capital);
        String h1Text = String.format("Is $%s in %s Enough for Monthly Income?", DOLLAR_FORMAT.format(capital),
                stock.getTicker());

        model.addAttribute("pageHeading", h1Text);

        addSeoFreshnessAttributes(model, pageTitle, pageDescription);

        // [SEO] Advanced Schema: Breadcrumb + Dataset (Income Mode)
        // Reusing same generator but adapting for Income mode logic if needed,
        // for simplicity we map income to monthlyAmount param context or overload.
        String schemaJson = generateSchemaJson(stock, monthlyIncome, capital);
        model.addAttribute("jsonLdSchema", schemaJson);

        // [SEO] FAQ Schema (Standalone, Spoiler-Free)
        String faqJson = generateFaqSchema(stock.getTicker(), "investment", capital);
        model.addAttribute("faqJson", faqJson);
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
        String monthYear = now.format(DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale.US));
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
                            }
                          ]
                        }
                        """,
                ticker, ticker, // Breadcrumb
                name, monthlyTarget, name, ticker, today // Dataset
        );
    }

    // [SEO Strategy] 1. Target Mode Title (Verification Style)
    private String generateTargetModeTitle(String ticker, double amount) {
        return String.format("Is $%s/mo Dividend from %s Realistic? (2026 Analysis)",
                DOLLAR_FORMAT.format(amount), ticker);
    }

    // [SEO Strategy] 2. Target Mode Description (No Spoilers)
    private String generateTargetModeDesc(String ticker, double amount) {
        return String.format(
                "We calculated exactly how much capital you need to generate $%s/month from %s. The amount depends on the current yield. See the realistic breakdown here.",
                DOLLAR_FORMAT.format(amount), ticker);
    }

    // [SEO Strategy] 3. Income Mode Title (Verification Style)
    private String generateIncomeModeTitle(String ticker, double capital) {
        return String.format("Can $%s in %s Really Generate Meaningful Income?",
                DOLLAR_FORMAT.format(capital), ticker);
    }

    // [SEO Strategy] 4. Income Mode Description (No Spoilers)
    private String generateIncomeModeDesc(String ticker, double capital) {
        return String.format(
                "We analyzed the real monthly income potential of investing $%s in %s. The results depend on yield fluctuations. Check the exact income report.",
                DOLLAR_FORMAT.format(capital), ticker);
    }

    // [SEO Strategy] 5. FAQ Gen (Calculator CTA)
    private String generateFaqSchema(String ticker, String contextType, double amount) {
        String question = contextType.equals("monthly income")
                ? String.format("How much %s stock do I need for $%s/mo?", ticker, DOLLAR_FORMAT.format(amount))
                : String.format("How much does $%s in %s pay per month?", DOLLAR_FORMAT.format(amount), ticker);

        return String.format(
                """
                        {
                          "@context": "https://schema.org",
                          "@type": "FAQPage",
                          "mainEntity": [{
                            "@type": "Question",
                            "name": "%s",
                            "acceptedAnswer": {
                              "@type": "Answer",
                              "text": "The exact result depends on the real-time dividend yield and tax assumptions. Our interactive calculator provides a detailed breakdown of the investment performance based on today's market data."
                            }
                          }]
                        }
                        """,
                question);
    }
}