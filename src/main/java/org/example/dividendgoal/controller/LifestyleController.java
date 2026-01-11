package org.example.dividendgoal.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.dividendgoal.model.LifestyleItem;
import org.example.dividendgoal.model.Stock;
import org.example.dividendgoal.service.DividendCalculationService;
import org.example.dividendgoal.service.LifestyleService;
import org.example.dividendgoal.service.StockDataService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
public class LifestyleController {

        private final StockDataService stockDataService;
        private final LifestyleService lifestyleService;
        private final DividendCalculationService calculationService;

        public LifestyleController(StockDataService stockDataService, LifestyleService lifestyleService,
                        DividendCalculationService calculationService) {
                this.stockDataService = stockDataService;
                this.lifestyleService = lifestyleService;
                this.calculationService = calculationService;
        }

        @GetMapping("/lifestyle/cost-of-{itemSlug}-vs-{ticker}-dividend")
        public String showLifestylePlan(
                        @PathVariable String itemSlug,
                        @PathVariable String ticker,
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

                // 3. 모델 바인딩 (StockController와 최대한 호환되게)
                model.addAttribute("stock", stock);
                model.addAttribute("item", item);
                model.addAttribute("monthlyAmount", monthlyCost);
                model.addAttribute("requiredInvestment", requiredInvestment);
                model.addAttribute("formattedRequiredInvestment", String.format("%,.0f", requiredInvestment)); // 포맷팅 추가
                model.addAttribute("calculationMode", "TARGET");

                // 4. [SEO] Canonical URL (중복 콘텐츠 방지용 절대 경로)
                String currentUrl = ServletUriComponentsBuilder.fromRequestUri(request).build().toUriString();
                model.addAttribute("currentUrl", currentUrl);

                // 5. [SEO] 동적 메타데이터 (고유성 강화)
                String pageTitle = String.format("Is %s Dividend Enough for %s? | Money First Analysis",
                                stock.getTicker(), item.getName());
                String pageDescription = String.format(
                                "Analysis: Can %s (%s) dividends cover your %s bill? Calculated required capital: $%.0f. See the full income report.",
                                stock.getName(), stock.getTicker(), item.getName(), requiredInvestment);

                // 6. [FIX] 누락된 content 객체 주입 (500 Error 방지)
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

                model.addAttribute("pageTitle", pageTitle);
                model.addAttribute("pageDescription", pageDescription);

                // 6. 뷰 이름 반환 (기존 result.html 재사용)
                return "result";
        }
}