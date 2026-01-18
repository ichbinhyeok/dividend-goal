package org.example.dividendgoal.controller;

import org.example.dividendgoal.AppConstants;
import org.example.dividendgoal.service.LifestyleService;
import org.example.dividendgoal.service.StockDataService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping(produces = MediaType.APPLICATION_XML_VALUE)
public class SitemapController {

    private final StockDataService stockDataService;
    private final LifestyleService lifestyleService;

    public SitemapController(StockDataService stockDataService, LifestyleService lifestyleService) {
        this.stockDataService = stockDataService;
        this.lifestyleService = lifestyleService;
    }

    // 1. 사이트맵 인덱스 (Sitemap Index)
    @GetMapping("/sitemap.xml")
    public ResponseEntity<String> sitemapIndex() {
        String baseUrl = AppConstants.BASE_URL;
        String xml = String.format("""
                <?xml version="1.0" encoding="UTF-8"?>
                <sitemapindex xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
                    <sitemap>
                        <loc>%s/sitemap-main.xml</loc>
                        <lastmod>%s</lastmod>
                    </sitemap>
                    <sitemap>
                        <loc>%s/sitemap-lifestyle.xml</loc>
                        <lastmod>%s</lastmod>
                    </sitemap>
                    <sitemap>
                        <loc>%s/sitemap-comparison.xml</loc>
                        <lastmod>%s</lastmod>
                    </sitemap>
                </sitemapindex>
                """, baseUrl, getMonthlyLastMod(), baseUrl, getMonthlyLastMod(), baseUrl, getMonthlyLastMod());
        return ResponseEntity.ok(xml.toString());
    }

    // 2. 메인 사이트맵 (기본 페이지 + 일부 계산 예시)
    @GetMapping("/sitemap-main.xml")
    public ResponseEntity<String> sitemapMain() {
        String baseUrl = AppConstants.BASE_URL;
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        xml.append(buildUrl(baseUrl + "/", "1.0"));
        xml.append(buildUrl(baseUrl + "/articles", "0.9"));
        xml.append(buildUrl(baseUrl + "/about", "0.5"));

        // [SEO] Static Articles (Ensure these are indexed)
        List<String> articles = List.of(
                "what-is-dividend-yield",
                "why-dividend-growth-matters",
                "schd-vs-jepi-comparison",
                "how-to-use-dividend-calculator",
                "dividend-income-vs-interest",
                "why-small-expenses-matter",
                "best-monthly-dividend-stocks");
        articles.forEach(slug -> xml.append(buildUrl(baseUrl + "/articles/" + slug, "0.8")));

        // [SEO] Only index 'Golden Amounts' to prevent thin/duplicate content
        // [SEO] Only index 'Golden Amounts' to prevent thin/duplicate content
        // Expanded aggressive list: $100, $300, $500, $1000, $1500, $2000, $3000, $5000
        // (Drop $10k per plan)
        List<Integer> amounts = List.of(100, 300, 500, 1000, 1500, 2000, 3000, 5000);

        stockDataService.getAvailableTickers().forEach(ticker -> amounts.forEach(amount -> xml.append(
                buildUrl(String.format("%s/how-much-dividend/%d-per-month/%s", baseUrl, amount, ticker), "0.8"))));

        xml.append("</urlset>");
        return ResponseEntity.ok(xml.toString());
    }

    // 3. 라이프스타일 위성 사이트맵 (인기 조합만 포함: ~120-140개 URL)
    @GetMapping("/sitemap-lifestyle.xml")
    public ResponseEntity<String> sitemapLifestyle() {
        String baseUrl = AppConstants.BASE_URL;
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        // [SEO] 인기 티커 정의 (12개)
        // [SEO] Use ALL tickers for Lifestyle combinations to maximize long-tail
        // coverage
        List<String> allTickers = stockDataService.getAvailableTickers();

        // [SEO] 인기 아이템 x 인기 티커 조합만 sitemap에 포함
        lifestyleService.getPopularItems().forEach(item -> {
            allTickers.forEach(ticker -> {
                String url = String.format("%s/lifestyle/cost-of-%s-vs-%s-dividend", baseUrl, item.getSlug(), ticker);
                xml.append(buildUrl(url, "0.9")); // 모두 높은 우선순위
            });
        });

        xml.append("</urlset>");
        return ResponseEntity.ok(xml.toString());
    }

    // 4. 비교(VS) 사이트맵 (Conflict Marketing)
    @GetMapping("/sitemap-comparison.xml")
    public ResponseEntity<String> sitemapComparison() {
        String baseUrl = AppConstants.BASE_URL;
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        List<String> tickers = stockDataService.getAvailableTickers();

        // Generate Pair Combinations (A vs B)
        // Order independent: SCHD-vs-JEPI only (don't do JEPI-vs-SCHD to avoid
        // duplicate content)
        for (int i = 0; i < tickers.size(); i++) {
            for (int j = i + 1; j < tickers.size(); j++) {
                String t1 = tickers.get(i);
                String t2 = tickers.get(j);
                String url = String.format("%s/compare/%s-vs-%s", baseUrl, t1, t2);
                xml.append(buildUrl(url, "0.8"));
            }
        }

        xml.append("</urlset>");
        return ResponseEntity.ok(xml.toString());
    }

    private String getMonthlyLastMod() {
        return LocalDate.now().withDayOfMonth(1).toString();
    }

    private String buildUrl(String location, String priority) {
        return String.format("<url><loc>%s</loc><lastmod>%s</lastmod><priority>%s</priority></url>", location,
                getMonthlyLastMod(), priority);
    }
}