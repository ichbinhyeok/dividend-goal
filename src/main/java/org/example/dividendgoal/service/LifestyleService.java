package org.example.dividendgoal.service;

import org.example.dividendgoal.model.LifestyleItem;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class LifestyleService {

    private final Random random = new Random();

    // 30개 이상의 롱테일 키워드 아이템 리스트
    private final List<LifestyleItem> items = List.of(
            // 구독 (Subscription)
            new LifestyleItem("netflix-premium", "Netflix Premium", 22.99, "Subscription"),
            new LifestyleItem("youtube-premium", "YouTube Premium", 13.99, "Subscription"),
            new LifestyleItem("spotify-family", "Spotify Family", 16.99, "Subscription"),
            new LifestyleItem("chatgpt-plus", "ChatGPT Plus", 20.00, "AI Tool"),
            new LifestyleItem("adobe-creative-cloud", "Adobe Creative Cloud", 54.99, "Subscription"),
            new LifestyleItem("disney-plus-bundle", "Disney+ Bundle", 14.99, "Subscription"),
            new LifestyleItem("amazon-prime", "Amazon Prime", 14.99, "Subscription"),

            // 전자기기 & 통신 (Gadgets & Bills)
            new LifestyleItem("iphone-16-pro-installment", "iPhone 16 Pro (Installment)", 41.62, "Gadget"),
            new LifestyleItem("macbook-pro-14", "MacBook Pro 14 (Monthly)", 166.00, "Gadget"),
            new LifestyleItem("samsung-s24-ultra", "Samsung S24 Ultra (Monthly)", 45.00, "Gadget"),
            new LifestyleItem("verizon-unlimited", "Verizon 5G Unlimited", 80.00, "Bill"),
            new LifestyleItem("starlink-internet", "Starlink Internet", 120.00, "Bill"),
            new LifestyleItem("att-fiber-internet", "AT&T Fiber Internet", 55.00, "Bill"),

            // 자동차 (Car)
            new LifestyleItem("tesla-model-3-lease", "Tesla Model 3 Lease", 329.00, "Car"),
            new LifestyleItem("tesla-model-y-lease", "Tesla Model Y Lease", 399.00, "Car"),
            new LifestyleItem("ford-f150-finance", "Ford F-150 Finance", 750.00, "Car"),
            new LifestyleItem("toyota-camry-lease", "Toyota Camry Lease", 350.00, "Car"),
            new LifestyleItem("honda-crv-lease", "Honda CR-V Lease", 340.00, "Car"),
            new LifestyleItem("car-insurance-avg", "Average Car Insurance", 150.00, "Car"),

            // 생활 (Lifestyle)
            new LifestyleItem("starbucks-daily", "Daily Starbucks", 150.00, "Lifestyle"),
            new LifestyleItem("gym-membership-luxury", "Luxury Gym Membership", 120.00, "Health"),
            new LifestyleItem("crossfit-membership", "CrossFit Membership", 160.00, "Health"),
            new LifestyleItem("weekly-groceries-single", "Weekly Groceries (Single)", 300.00, "Lifestyle"),
            new LifestyleItem("weekly-groceries-family", "Weekly Groceries (Family)", 800.00, "Lifestyle"),
            new LifestyleItem("weekend-dining-out", "Weekend Dining Out", 200.00, "Lifestyle"),
            new LifestyleItem("pet-food-monthly", "Monthly Pet Food", 80.00, "Lifestyle"),

            // 주거 (Housing)
            new LifestyleItem("avg-rent-nyc", "Average Rent in NYC", 3500.00, "Housing"),
            new LifestyleItem("avg-rent-texas", "Average Rent in Texas", 1200.00, "Housing"),
            new LifestyleItem("avg-rent-florida", "Average Rent in Florida", 1500.00, "Housing"),
            new LifestyleItem("hoa-fees", "HOA Fees", 300.00, "Housing")
    );

    public List<LifestyleItem> getAllItems() {
        return items;
    }

    public Optional<LifestyleItem> findBySlug(String slug) {
        return items.stream().filter(i -> i.getSlug().equalsIgnoreCase(slug)).findFirst();
    }

    public LifestyleItem getRandomItem() {
        return items.get(random.nextInt(items.size()));
    }
}