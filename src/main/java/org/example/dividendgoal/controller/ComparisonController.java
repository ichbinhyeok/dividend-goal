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

                // 5. SEO Meta
                String monthYear = LocalDate.now()
                                .format(DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale.US));
                String title = String.format("%s vs %s Dividend: Which is Better? (Updated %s)",
                                s1.getTicker(), s2.getTicker(), monthYear);
                String desc = String.format(
                                "Compare %s (%.2f%% Yield) vs %s (%.2f%% Yield). See which dividend stock wins on Yield, Growth, and Safety. Updated %s.",
                                s1.getTicker(), s1.getYield(), s2.getTicker(), s2.getYield(), monthYear);

                model.addAttribute("pageTitle", title);
                model.addAttribute("pageDescription", desc);
                model.addAttribute("currentDate", LocalDate.now().toString());

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
}
