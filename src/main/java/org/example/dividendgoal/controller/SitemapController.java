package org.example.dividendgoal.controller;

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
    private static final List<Integer> AMOUNTS = List.of(500, 1000, 1500, 2000, 3000, 5000, 7500, 10000);

    public SitemapController(StockDataService stockDataService) {
        this.stockDataService = stockDataService;
    }

    @GetMapping("/sitemap.xml")
    public ResponseEntity<String> sitemap() {
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        StringBuilder xml = new StringBuilder();
        xml.append("""
                <?xml version="1.0" encoding="UTF-8"?>
                <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
                """);

        xml.append(buildUrl(baseUrl + "/"));

        stockDataService.getAvailableTickers().forEach(ticker ->
                AMOUNTS.forEach(amount -> {
                    String url = String.format("%s/how-much-dividend/%d-per-month/%s", baseUrl, amount, ticker);
                    xml.append(buildUrl(url));
                })
        );

        xml.append("</urlset>");
        return ResponseEntity.ok(xml.toString());
    }

    private String buildUrl(String location) {
        return String.format("""
                <url>
                    <loc>%s</loc>
                    <lastmod>%s</lastmod>
                </url>
                """, location, LocalDate.now());
    }
}
