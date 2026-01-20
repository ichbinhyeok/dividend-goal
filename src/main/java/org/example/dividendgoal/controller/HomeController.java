package org.example.dividendgoal.controller;

import org.example.dividendgoal.service.StockDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
    private final StockDataService stockDataService;

    public HomeController(StockDataService stockDataService) {
        this.stockDataService = stockDataService;
    }

    @GetMapping("/")
    public String home(HttpServletResponse response, Model model) {
        setCacheControl(response);
        model.addAttribute("stocks", stockDataService.getAllStocks());
        String pageTitle = "Dividend Income Guide and Calculator | Money First";
        String pageDescription = "Learn how monthly dividend income targets translate into estimated capital requirements.";
        addSeoFreshnessAttributes(model, pageTitle, pageDescription);
        return "home";
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
        // [Performance] Homepage: 5-minute browser caching (balances freshness &
        // performance)
        response.setHeader("Cache-Control", "public, max-age=300"); // 5 minutes
    }

    @PostMapping("/calculate")
    public String calculate(@RequestParam("amount") double amount,
            @RequestParam("ticker") String ticker,
            RedirectAttributes redirectAttributes) {

        if (ticker == null || ticker.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Please enter a ticker symbol.");
            return "redirect:/";
        }

        String cleanedTicker = ticker.trim().toUpperCase();

        // [수정] 티커 존재 여부 확인 및 로그 기록
        if (stockDataService.findByTicker(cleanedTicker).isEmpty()) {
            stockDataService.logMissingTicker(cleanedTicker);
            redirectAttributes.addFlashAttribute("error",
                    "The ticker '" + cleanedTicker + "' is not supported yet. We have logged your request!");
            return "redirect:/";
        }

        if (amount <= 0) {
            redirectAttributes.addFlashAttribute("error", "Please enter a positive monthly amount.");
            return "redirect:/";
        }

        long rounded = Math.round(amount);
        return "redirect:/how-much-dividend/" + rounded + "-per-month/" + cleanedTicker;
    }
}