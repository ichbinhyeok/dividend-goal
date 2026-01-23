package org.example.dividendgoal.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.example.dividendgoal.model.Stock;
import org.example.dividendgoal.service.StockDataService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
public class ComparisonController {

        private final StockDataService stockDataService;
        private static final DecimalFormat PCT_FORMAT = new DecimalFormat("#.##");

        public ComparisonController(StockDataService stockDataService) {
                this.stockDataService = stockDataService;
        }

        @GetMapping("/compare/{ticker1}-vs-{ticker2}")
        public String compareStocks(@PathVariable("ticker1") String ticker1, @PathVariable("ticker2") String ticker2,
                        HttpServletResponse response, Model model) {

                // 1. Validate Tickers
                Stock s1 = stockDataService.findByTicker(ticker1)
                                .orElseThrow(
                                                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                                "Ticker " + ticker1 + " not found"));
                Stock s2 = stockDataService.findByTicker(ticker2)
                                .orElseThrow(
                                                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                                "Ticker " + ticker2 + " not found"));

                if (s1.getTicker().equalsIgnoreCase(s2.getTicker())) {
                        return "redirect:/how-much-dividend/1000-per-month/" + s1.getTicker();
                }

                // 2. Set Headers (Monthly Freshness)
                setCacheControl(response);

                // 3. Compare Logic
                model.addAttribute("s1", s1);
                model.addAttribute("s2", s2);

                // Winner Logic
                model.addAttribute("yieldWinner",
                                s1.getYield() > s2.getYield() ? s1 : (s2.getYield() > s1.getYield() ? s2 : null));
                model.addAttribute("growthWinner", s1.getDividendGrowth() > s2.getDividendGrowth() ? s1
                                : (s2.getDividendGrowth() > s1.getDividendGrowth() ? s2 : null));

                // 4. Schema.org (Table)
                String schemaJson = generateComparisonSchema(s1, s2);
                model.addAttribute("jsonLdSchema", schemaJson);

                // 5. SEO Meta (Condition-based Choice Strategy - Expert Feedback Implemented)
                String pageTitle = generateConditionBasedTitle(s1, s2);
                String pageDescription = generateConditionBasedDesc(s1, s2);
                String h1Text = String.format("%s vs %s: High Income or Long-Term Stability?", s1.getTicker(),
                                s2.getTicker());

                model.addAttribute("pageHeading", h1Text);
                model.addAttribute("pageTitle", pageTitle);
                model.addAttribute("pageDescription", pageDescription);
                model.addAttribute("currentDate", LocalDate.now().toString());

                // 6. FAQ Schema (Spoiler-free)
                String faqJson = generateFaqSchema(s1, s2);
                model.addAttribute("faqJson", faqJson);

                return "compare"; // New Template needed
        }

        private void setCacheControl(HttpServletResponse response) {
                // [Performance] Comparison pages: 5-minute browser caching
                response.setHeader("Cache-Control", "public, max-age=300"); // 5 minutes

                LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
                long lastModTime = java.sql.Timestamp.valueOf(firstDayOfMonth.atStartOfDay()).getTime();
                response.setDateHeader("Last-Modified", lastModTime);
        }

        private String generateComparisonSchema(Stock s1, Stock s2) {
                return String.format("""
                                {
                                  "@context": "https://schema.org",
                                  "@type": "Table",
                                  "about": "Dividend Comparison: %s vs %s",
                                  "description": "Side-by-side comparison of dividend yield, growth, and risk.",
                                  "dateModified": "%s"
                                }
                                """, s1.getTicker(), s2.getTicker(), LocalDate.now().toString());
        }

        // [SEO Strategy] Condition-based Title
        private String generateConditionBasedTitle(Stock s1, Stock s2) {
                String monthYear = LocalDate.now()
                                .format(DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale.US));
                return String.format("%s vs %s: High Income or Long-Term Stability? (%s Analysis)",
                                s1.getTicker(), s2.getTicker(), monthYear);
        }

        // [SEO Strategy] Condition-based Description (Trade-off focus)
        private String generateConditionBasedDesc(Stock s1, Stock s2) {
                String monthYear = LocalDate.now()
                                .format(DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale.US));
                return String.format(
                                "Updated %s | We compared the dividend yield, growth streak, and risk profiles of %s and %s. One pays higher immediate income, while the other offers better long-term growth. See the full scorecard.",
                                monthYear, s1.getTicker(), s2.getTicker());
        }

        // [SEO Strategy] FAQ (No Winner Declared)
        private String generateFaqSchema(Stock s1, Stock s2) {
                return String.format(
                                """
                                                {
                                                  "@context": "https://schema.org",
                                                  "@type": "FAQPage",
                                                  "mainEntity": [{
                                                    "@type": "Question",
                                                    "name": "Which is better for dividends, %s or %s?",
                                                    "acceptedAnswer": {
                                                      "@type": "Answer",
                                                      "text": "It strictly depends on your financial goal (Income vs Growth). Our comparison tool analyzes real-time data to help you decide which fits your portfolio better based on current yield and safety scores."
                                                    }
                                                  }]
                                                }
                                                """,
                                s1.getTicker(), s2.getTicker());
        }
}
