package org.example.dividendgoal.controller;

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
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
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
                </sitemapindex>
                """, baseUrl, LocalDate.now(), baseUrl, LocalDate.now());
        return ResponseEntity.ok(xml.toString());
    }

    // 2. 메인 사이트맵 (기본 페이지 + 일부 계산 예시)
    @GetMapping("/sitemap-main.xml")
    public ResponseEntity<String> sitemapMain() {
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        xml.append(buildUrl(baseUrl + "/", "1.0"));
        xml.append(buildUrl(baseUrl + "/articles", "0.9"));
        xml.append(buildUrl(baseUrl + "/about", "0.5"));

        // 기존 숫자 기반 URL (대표적인 것만)
        List<Integer> amounts = List.of(500, 1000, 3000);
        stockDataService.getAvailableTickers().forEach(ticker ->
                amounts.forEach(amount ->
                        xml.append(buildUrl(String.format("%s/how-much-dividend/%d-per-month/%s", baseUrl, amount, ticker), "0.8"))
                )
        );

        xml.append("</urlset>");
        return ResponseEntity.ok(xml.toString());
    }

    // 3. 라이프스타일 위성 사이트맵 (3,300개 낚싯바늘)
    @GetMapping("/sitemap-lifestyle.xml")
    public ResponseEntity<String> sitemapLifestyle() {
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        List<String> popularTickers = List.of("AAPL", "SCHD", "O", "JEPI", "TSLA", "NVDA", "MSFT", "KO");

        lifestyleService.getAllItems().forEach(item -> {
            stockDataService.getAvailableTickers().forEach(ticker -> {
                String priority = popularTickers.contains(ticker) ? "0.9" : "0.6";
                String url = String.format("%s/lifestyle/cost-of-%s-vs-%s-dividend", baseUrl, item.getSlug(), ticker);
                xml.append(buildUrl(url, priority));
            });
        });

        xml.append("</urlset>");
        return ResponseEntity.ok(xml.toString());
    }

    private String buildUrl(String location, String priority) {
        return String.format("<url><loc>%s</loc><lastmod>%s</lastmod><priority>%s</priority></url>", location, LocalDate.now(), priority);
    }
}