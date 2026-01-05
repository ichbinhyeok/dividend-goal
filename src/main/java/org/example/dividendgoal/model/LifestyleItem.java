package org.example.dividendgoal.model;

public class LifestyleItem {
    private String slug;      // URL용 (예: netflix-premium)
    private String name;      // 화면 표시용 (예: Netflix Premium)
    private double cost;      // 월 비용 (예: 15.49)
    private String category;  // 카테고리 (Subscription, Car 등)

    public LifestyleItem(String slug, String name, double cost, String category) {
        this.slug = slug;
        this.name = name;
        this.cost = cost;
        this.category = category;
    }

    public String getSlug() { return slug; }
    public String getName() { return name; }
    public double getCost() { return cost; }
    public String getCategory() { return category; }
}