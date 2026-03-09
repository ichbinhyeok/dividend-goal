package org.example.dividendgoal.controller;

import org.example.dividendgoal.AppConstants;
import org.example.dividendgoal.seo.SeoPolicy;
import org.example.dividendgoal.service.StockDataService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping(produces = MediaType.APPLICATION_XML_VALUE)
public class SitemapController {

    private final StockDataService stockDataService;

    public SitemapController(StockDataService stockDataService) {
        this.stockDataService = stockDataService;
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

        SeoPolicy.getCoreStaticPaths()
                .forEach(path -> xml.append(buildUrl(joinUrl(baseUrl, path), "/".equals(path) ? "1.0" : "0.7")));

        SeoPolicy.getArticleSlugs()
                .forEach(slug -> xml.append(buildUrl(baseUrl + "/articles/" + slug, "0.8")));

        List<String> indexableTickers = SeoPolicy.getIndexableTickersForTargets(stockDataService.getAvailableTickers());
        for (String ticker : indexableTickers) {
            for (Integer amount : SeoPolicy.getIndexableTargetAmounts()) {
                String url = String.format("%s/how-much-dividend/%d-per-month/%s", baseUrl, amount, ticker);
                String priority = amount == 1000 || amount == 2000 ? "0.9" : "0.7";
                xml.append(buildUrl(url, priority));
            }
        }

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

        List<String> lifestyleTickers = SeoPolicy.getIndexableTickersForLifestyle(stockDataService.getAvailableTickers());
        for (String itemSlug : SeoPolicy.getIndexableLifestyleItemSlugs()) {
            for (String ticker : lifestyleTickers) {
                String url = String.format("%s/lifestyle/cost-of-%s-vs-%s-dividend", baseUrl, itemSlug, ticker);
                xml.append(buildUrl(url, "0.6"));
            }
        }

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

        SeoPolicy.getIndexableComparisonPairs(stockDataService.getAvailableTickers())
                .forEach(pair -> xml.append(buildUrl(
                        String.format("%s/compare/%s-vs-%s", baseUrl, pair.left(), pair.right()),
                        "0.6")));

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

    private String joinUrl(String baseUrl, String path) {
        if ("/".equals(path)) {
            return baseUrl + "/";
        }
        return baseUrl + path;
    }
}
