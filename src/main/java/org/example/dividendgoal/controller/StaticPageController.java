package org.example.dividendgoal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StaticPageController {

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("pageTitle", "About Money First | Dividend Income Insights");
        model.addAttribute("pageDescription", "Learn about Money First, a server-rendered dividend income calculator focused on clarity, privacy, and educational use only.");
        return "about";
    }

    @GetMapping("/privacy-policy")
    public String privacy(Model model) {
        model.addAttribute("pageTitle", "Privacy Policy | Money First Dividend Calculator");
        model.addAttribute("pageDescription", "Understand how Money First operates without logins, accounts, or personal data storage.");
        return "privacy";
    }

    @GetMapping("/disclaimer")
    public String disclaimer(Model model) {
        model.addAttribute("pageTitle", "Disclaimer | Money First Dividend Calculator");
        model.addAttribute("pageDescription", "Important educational and informational disclaimer for the Money First dividend calculator.");
        return "disclaimer";
    }
}