package org.example.dividendgoal.controller;

import org.example.dividendgoal.service.StockDataService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    private final StockDataService stockDataService;

    public HomeController(StockDataService stockDataService) {
        this.stockDataService = stockDataService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("stocks", stockDataService.getAllStocks());
        model.addAttribute("pageTitle", "Dividend Income Guide and Calculator | Money First");
        model.addAttribute("pageDescription", "Learn how monthly dividend income targets translate into estimated capital requirements with a server-rendered, SEO-friendly calculator.");
        return "home";
    }

    @PostMapping("/calculate")
    public String calculate(@RequestParam("amount") double amount,
                            @RequestParam("ticker") String ticker,
                            RedirectAttributes redirectAttributes) {
        if (amount <= 0 || ticker == null || ticker.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Please enter a monthly amount and choose a ticker.");
            return "redirect:/";
        }
        long rounded = Math.round(amount);
        String cleanedTicker = ticker.trim().toUpperCase();
        return "redirect:/how-much-dividend/" + rounded + "-per-month/" + cleanedTicker;
    }
}