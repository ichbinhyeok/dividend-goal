package org.example.dividendgoal.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.dividendgoal.AppConstants;
import org.example.dividendgoal.model.LifestyleItem;
import org.example.dividendgoal.model.Stock;
import org.example.dividendgoal.service.DividendCalculationService;
import org.example.dividendgoal.service.DripSimulationService;
import org.example.dividendgoal.service.LifestyleService;
import org.example.dividendgoal.service.StockDataService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.text.DecimalFormat;

@Controller
public class LifestyleController {

        private final StockDataService stockDataService;
        private final LifestyleService lifestyleService;
        private final DividendCalculationService calculationService;
        private final DripSimulationService dripSimulationService;
        private final Random random = new Random();
        private static final DecimalFormat DOLLAR_FORMAT = new DecimalFormat("#,###.##");

        // [SEO] 인기 티커 (sitemap 포함 + index 허용)
        private static final List<String> POPULAR_TICKERS = List.of(
                        "AAPL", "SCHD", "O", "JEPI", "TSLA", "NVDA", "MSFT", "KO", "PEP", "JNJ", "PG", "VZ");

        public LifestyleController(StockDataService stockDataService, LifestyleService lifestyleService,
                        DividendCalculationService calculationService, DripSimulationService dripSimulationService) {
                this.stockDataService = stockDataService;
                this.lifestyleService = lifestyleService;
                this.calculationService = calculationService;
                this.dripSimulationService = dripSimulationService;
        }

        @GetMapping("/lifestyle/cost-of-{itemSlug}-vs-{ticker}-dividend")
        public String showLifestylePlan(
                        @PathVariable("itemSlug") String itemSlug,
                        @PathVariable("ticker") String ticker,
                        HttpServletRequest request,
                        Model model) {

                // 1. 데이터 조회
                LifestyleItem item = lifestyleService.findBySlug(itemSlug)
                                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemSlug));

                Stock stock = stockDataService.findByTicker(ticker)
                                .orElseThrow(() -> new IllegalArgumentException("Ticker not found: " + ticker));

                // 2. 계산 로직
                double monthlyCost = item.getCost();
                double requiredInvestment = calculationService.calculateRequiredInvestment(monthlyCost,
                                stock.getYield());

                boolean isDataAvailable = stock.getYield() > 0;
                model.addAttribute("isDataAvailable", isDataAvailable);

                // 3. [SEO] 인기 조합 여부 판단 (인기 아이템 + 인기 티커만 index 허용)
                boolean shouldIndex = item.isPopular() && POPULAR_TICKERS.contains(ticker.toUpperCase());
                model.addAttribute("shouldIndex", shouldIndex);

                // 4. 모델 바인딩 (StockController와 최대한 호환되게)
                model.addAttribute("stock", stock);
                model.addAttribute("item", item);
                model.addAttribute("monthlyAmount", monthlyCost);
                model.addAttribute("requiredInvestment", requiredInvestment);
                model.addAttribute("formattedRequiredInvestment", DOLLAR_FORMAT.format(requiredInvestment));
                model.addAttribute("calculationMode", "TARGET");
                model.addAttribute("stocks", stockDataService.getAllStocks());

                if (isDataAvailable) {
                        model.addAttribute("dripProjections",
                                        dripSimulationService.simulate(requiredInvestment, stock.getYield()));
                        model.addAttribute("lifestyleMeaning", getLifestyleComment(monthlyCost));

                        // Time Machine
                        if (stock.getDividendGrowth() > 0) {
                                List<Map<String, Object>> timeMachine = new ArrayList<>();
                                int[] yearsToCheck = { 1, 3, 5, 10 };
                                for (int year : yearsToCheck) {
                                        double futureCap = calculationService.calculateHypotheticalCapital(
                                                        monthlyCost, stock.getYield(), stock.getDividendGrowth(), year);
                                        double savedMoney = requiredInvestment - futureCap;

                                        Map<String, Object> data = new HashMap<>();
                                        data.put("year", year);
                                        data.put("capital", futureCap);
                                        data.put("saved", savedMoney);
                                        timeMachine.add(data);
                                }
                                model.addAttribute("timeMachine", timeMachine);
                        }

                        // Internal Linking
                        LifestyleItem recommendedItem = lifestyleService.getRandomItem();
                        model.addAttribute("recommendedItem", recommendedItem);
                }

                // 5. [SEO] Canonical URL (중복 콘텐츠 방지용 절대 경로)
                String currentUrl = AppConstants.BASE_URL + request.getRequestURI();
                model.addAttribute("currentUrl", currentUrl);

                // 6. [SEO] 동적 메타데이터 (고유성 강화)
                String pageTitle = String.format("Is %s Dividend Enough for %s? | Money First Analysis",
                                stock.getTicker(), item.getName());
                String pageDescription = String.format(
                                "Analysis: Can %s (%s) dividends cover your %s bill? Calculated required capital: $%.0f. See the full income report.",
                                stock.getName(), stock.getTicker(), item.getName(), requiredInvestment);

                // 7. [FIX] 누락된 content 객체 주입 (500 Error 방지)
                java.util.Map<String, String> content = new java.util.HashMap<>();
                content.put("introduction", String.format(
                                "Stop paying out of pocket for %s. Let your assets pay for it.", item.getName()));
                content.put("whatIsTicker", String.format("%s (%s) is a dividend-paying stock in the %s sector.",
                                stock.getName(), stock.getTicker(), stock.getSector()));
                content.put("investingAngle", String.format(
                                "With a yield of %.2f%%, %s is a candidate for income-focused portfolios.",
                                stock.getYield(), stock.getTicker()));
                content.put("disclaimer",
                                "Disclaimer: This is for informational purposes only. Past performance does not guarantee future results.");
                model.addAttribute("content", content);

                addSeoFreshnessAttributes(model, pageTitle, pageDescription);

                // [SEO] Internal Linking: Similar stocks
                List<Stock> similarStocks = stockDataService.getSimilarStocks(stock.getSector(), stock.getTicker(), 4);
                model.addAttribute("similarStocks", similarStocks);

                // 8. 뷰 이름 반환 (기존 result.html 재사용)
                return "result";
        }

        private void addSeoFreshnessAttributes(Model model, String baseTitle, String baseDescription) {
                LocalDate now = LocalDate.now();
                String monthYear = now.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
                String refreshText = "Updated " + monthYear;

                model.addAttribute("currentYear", now.getYear());
                model.addAttribute("currentDate", now.format(DateTimeFormatter.ISO_LOCAL_DATE));
                model.addAttribute("refreshText", refreshText);

                model.addAttribute("pageTitle", baseTitle + " (" + refreshText + ")");
                model.addAttribute("pageDescription", refreshText + " | " + baseDescription);
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
}