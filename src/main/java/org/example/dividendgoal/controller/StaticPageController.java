package org.example.dividendgoal.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.dividendgoal.seo.CanonicalUrls;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StaticPageController {

    @GetMapping("/about")
    public String about(Model model) {
        addSeoFreshnessAttributes(model, "About Money First",
                "Learn about our dividend calculator, privacy, and educational mission.");
        return "about";
    }

    @GetMapping("/methodology")
    public String methodology(HttpServletRequest request, Model model) {
        model.addAttribute("currentUrl", CanonicalUrls.fromRequest(request));

        addSeoFreshnessAttributes(model, "Dividend Calculation Methodology",
                "Transparent breakdown of our formulas and data sources.");
        return "methodology";
    }

    @GetMapping("/privacy-policy")
    public String privacy(Model model) {
        addSeoFreshnessAttributes(model, "Privacy Policy", "How Money First operates without personal data storage.");
        return "privacy";
    }

    @GetMapping("/disclaimer")
    public String disclaimer(Model model) {
        addSeoFreshnessAttributes(model, "Disclaimer", "Important educational and informational disclaimer.");
        return "disclaimer";
    }

    private void addSeoFreshnessAttributes(Model model, String baseTitle, String baseDescription) {
        java.time.LocalDate now = java.time.LocalDate.now();
        String monthYear = now.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale.US));
        model.addAttribute("currentYear", now.getYear());
        model.addAttribute("refreshText", "Updated " + monthYear);
        model.addAttribute("pageTitle", baseTitle + " | Money First");
        model.addAttribute("pageDescription", baseDescription + " (Updated " + monthYear + ")");
    }
}
