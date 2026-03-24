package org.example.dividendgoal.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.dividendgoal.model.LifestyleItem;
import org.example.dividendgoal.model.Stock;
import org.example.dividendgoal.seo.CanonicalUrls;
import org.example.dividendgoal.seo.SeoPolicy;
import org.example.dividendgoal.service.DividendCalculationService;
import org.example.dividendgoal.service.DripSimulationService;
import org.example.dividendgoal.service.LifestyleService;
import org.example.dividendgoal.service.StockDataService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

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
                        HttpServletResponse response,
                        Model model) {

                // 1. 데이터 조회
                LifestyleItem item = lifestyleService.findBySlug(itemSlug)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Item not found: " + itemSlug));

                Stock stock = stockDataService.findByTicker(ticker)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Ticker not found: " + ticker));

                if (!SeoPolicy.isIndexableLifestylePage(item.getSlug(), stock.getTicker())) {
                        throw new ResponseStatusException(HttpStatus.GONE,
                                        "Lifestyle page removed from the canonical SEO surface");
                }

                // 2. 계산 로직
                double monthlyCost = item.getCost();
                double requiredInvestment = calculationService.calculateRequiredInvestment(monthlyCost,
                                stock.getYield());

                boolean isDataAvailable = stock.getYield() > 0;
                model.addAttribute("isDataAvailable", isDataAvailable);

                // 3. [SEO] 인기 조합 여부 판단 (인기 아이템 + 인기 티커만 index 허용)
                boolean shouldIndex = item.isPopular() && SeoPolicy.isIndexableLifestylePage(item.getSlug(), ticker);
                model.addAttribute("shouldIndex", shouldIndex);
                applyRobotsHeader(response, shouldIndex);

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
                }

                // 5. [SEO] Canonical URL (중복 콘텐츠 방지용 절대 경로)
                model.addAttribute("currentUrl", CanonicalUrls.fromRequest(request));

                // 6. [SEO] 동적 메타데이터 (고유성 강화 + CTR 최적화: 정답 숨기기)
                // [FIX] Zero-Click 방지: 결과 금액($)과 확정적 표현을 제거하고, 질문형/검증형 톤으로 변경
                String pageTitle = generateVerificationTitle(stock.getTicker(), item.getName());
                String pageDescription = generateCuriosityDescription(stock.getTicker(), item.getName());

                // [SEO] FAQ Schema (SERP 점유율 확대)
                String faqJson = generateFaqSchema(stock.getTicker(), item.getName());
                model.addAttribute("faqJson", faqJson);

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

                // 8. 뷰 이름 반환 (기존 result.html 재사용)
                return "result";
        }

        private void applyRobotsHeader(HttpServletResponse response, boolean shouldIndex) {
                if (!shouldIndex) {
                        response.setHeader("X-Robots-Tag", "noindex, follow");
                }
        }

        private void addSeoFreshnessAttributes(Model model, String baseTitle, String baseDescription) {
                LocalDate now = LocalDate.now();
                String monthYear = now.format(DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale.US));
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

        // [SEO Strategy] Verification Title (No Clickbait, No "Analysis" bore)
        private String generateVerificationTitle(String ticker, String itemName) {
                return String.format("Can %s Dividends Really Pay for Your %s Subscription?", ticker, itemName);
        }

        // [SEO Strategy] Curiosity Gap Description (Hides the number, sells the tool)
        private String generateCuriosityDescription(String ticker, String itemName) {
                return String.format(
                                "We analyzed whether %s dividends can realistically cover your %s bill. The required investment depends on current yield assumptions. Check the exact income breakdown with our calculator.",
                                ticker, itemName);
        }

        // [SEO Strategy] FAQ Schema for SERP Domination (Directs to calculator)
        private String generateFaqSchema(String ticker, String itemName) {
                return String.format(
                                """
                                                {
                                                  "@context": "https://schema.org",
                                                  "@type": "FAQPage",
                                                  "mainEntity": [{
                                                    "@type": "Question",
                                                    "name": "How much %s stock do I need to cover %s?",
                                                    "acceptedAnswer": {
                                                      "@type": "Answer",
                                                      "text": "The exact investment required depends on the current dividend yield and tax assumptions. Our interactive calculator provides a detailed breakdown of the capital needed to offset this cost completely based on today's market data."
                                                    }
                                                  }]
                                                }
                                                """,
                                ticker, itemName);
        }
}
