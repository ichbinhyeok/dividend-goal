package org.example.dividendgoal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ContactController {

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("pageTitle", "Contact Us | Money First");
        model.addAttribute("pageDescription",
                "Get in touch with the Money First team. Questions, suggestions, or feedback about our dividend calculator? We'd love to hear from you.");
        return "contact";
    }
}
