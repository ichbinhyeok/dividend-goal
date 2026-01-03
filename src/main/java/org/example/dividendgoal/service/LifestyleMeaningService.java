package org.example.dividendgoal.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LifestyleMeaningService {

    public String describe(double monthlyIncome) {
        List<String> items = new ArrayList<>();

        if (monthlyIncome >= 1200) {
            items.add("a meaningful share of monthly rent in many U.S. cities");
        }
        if (monthlyIncome >= 800) {
            items.add("a broad grocery run each week");
        }
        if (monthlyIncome >= 500) {
            items.add("typical gas expenses for a commuter or two");
        }
        if (monthlyIncome >= 200) {
            items.add("a family phone plan and streaming bundle");
        }
        if (monthlyIncome >= 150) {
            items.add("Netflix Premium and multiple music subscriptions");
        }
        if (monthlyIncome >= 100) {
            items.add("weekday Starbucks coffee or tea runs");
        }
        if (monthlyIncome >= 50) {
            items.add("a few rideshares or transit passes");
        }

        if (items.isEmpty()) {
            return "This amount could chip away at small recurring bills like a streaming plan or a couple of coffees each week.";
        }

        StringBuilder description = new StringBuilder("At roughly $")
                .append(String.format("%.0f", monthlyIncome))
                .append(" per month, this could cover ");

        for (int i = 0; i < items.size(); i++) {
            if (i > 0 && i == items.size() - 1) {
                description.append(" and ");
            } else if (i > 0) {
                description.append(", ");
            }
            description.append(items.get(i));
        }
        description.append(". Illustration only—real budgets vary.");
        return description.toString();
    }
}
