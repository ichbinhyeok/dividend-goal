package org.example.dividendgoal.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.dividendgoal.seo.CanonicalUrls;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * [SEO] Global attributes applied to all pages
 */
@ControllerAdvice
public class GlobalSeoAdvice {

    @ModelAttribute
    public void addAttributes(Model model, HttpServletRequest request) {
        // Freshness attributes
        LocalDate now = LocalDate.now();
        String monthYear = now.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        String refreshText = "Updated " + monthYear;
        model.addAttribute("currentYear", now.getYear());
        model.addAttribute("currentDate", now.format(DateTimeFormatter.ISO_LOCAL_DATE));
        model.addAttribute("refreshText", refreshText);

        // [SEO] Canonical URL for all pages (prevents duplicate content issues)
        model.addAttribute("canonicalUrl", CanonicalUrls.fromRequest(request));
    }
}
