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

@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
    private final StockDataService stockDataService;

    public HomeController(StockDataService stockDataService) {
        this.stockDataService = stockDataService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("stocks", stockDataService.getAllStocks());
        model.addAttribute("pageTitle", "Dividend Income Guide and Calculator | Money First");
        model.addAttribute("pageDescription", "Learn how monthly dividend income targets translate into estimated capital requirements.");
        return "home";
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
            redirectAttributes.addFlashAttribute("error", "The ticker '" + cleanedTicker + "' is not supported yet. We have logged your request!");
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