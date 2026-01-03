package org.example.dividendgoal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/articles")
public class ArticleController {

    @GetMapping("")
    public String index(Model model) {
        model.addAttribute("pageTitle", "Dividend Investing Guides | Money First");
        model.addAttribute("pageDescription", "Learn the basics of dividend investing, yield calculation, and growth strategies.");
        return "articles/index";
    }

    @GetMapping("/what-is-dividend-yield")
    public String dividendYield(Model model) {
        model.addAttribute("pageTitle", "What is Dividend Yield? A Simple Guide");
        model.addAttribute("pageDescription", "Understand the definition of dividend yield, how it works, and why it matters for your income goals.");
        return "articles/what-is-dividend-yield";
    }

    @GetMapping("/why-dividend-growth-matters")
    public String dividendGrowth(Model model) {
        model.addAttribute("pageTitle", "Why Dividend Growth Matters for Long-Term Wealth");
        model.addAttribute("pageDescription", "Discover why growing dividends are crucial for beating inflation and compounding wealth.");
        return "articles/why-dividend-growth-matters";
    }

    @GetMapping("/schd-vs-jepi-comparison")
    public String schdVsJepi(Model model) {
        model.addAttribute("pageTitle", "SCHD vs JEPI: A Simple Comparison for Investors");
        model.addAttribute("pageDescription", "Compare SCHD (Growth) and JEPI (Income) to decide which ETF fits your financial goals.");
        return "articles/schd-vs-jepi-comparison";
    }

    @GetMapping("/how-to-use-dividend-calculator")
    public String howToUse(Model model) {
        model.addAttribute("pageTitle", "How to Use the Dividend Goal Calculator");
        model.addAttribute("pageDescription", "A step-by-step guide to calculating the capital required for your monthly passive income target.");
        return "articles/how-to-use-dividend-calculator";
    }

    @GetMapping("/dividend-income-vs-interest")
    public String dividendVsInterest(Model model) {
        model.addAttribute("pageTitle", "Dividend Income vs. Interest Income: What's the Difference?");
        model.addAttribute("pageDescription", "Explore the key differences between stock dividends and bank interest, including risks and tax benefits.");
        return "articles/dividend-income-vs-interest";
    }
}