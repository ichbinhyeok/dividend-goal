package org.example.dividendgoal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AboutController {

    @GetMapping("/about/methodology")
    public String showMethodology(Model model) {
        model.addAttribute("pageTitle", "Calculation Methodology | Money First");
        model.addAttribute("pageDescription",
                "How we calculate dividend safety, required capital, and yield projections.");
        return "methodology"; // Simple static template
    }
}
