package org.example.dividendgoal.controller;

import org.example.dividendgoal.service.StockDataService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(produces = MediaType.APPLICATION_XML_VALUE)
public class SitemapController {

    private final StockDataService stockDataService;

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

        // 메인 페이지
        xml.append(buildUrl(baseUrl + "/"));
        xml.append(buildUrl(baseUrl + "/about"));
        xml.append(buildUrl(baseUrl + "/privacy-policy"));
        xml.append(buildUrl(baseUrl + "/disclaimer"));

        // 1. 월 배당 목표 계산기 URL (수천 개 생성)
        List<Integer> amounts = generateAmounts();
        stockDataService.getAvailableTickers().forEach(ticker ->
                amounts.forEach(amount -> {
                    String url = String.format("%s/how-much-dividend/%d-per-month/%s", baseUrl, amount, ticker);
                    xml.append(buildUrl(url));
                })
        );

        // 2. 투자금 역산 계산기 URL (수천 개 생성)
        List<Integer> capitals = generateCapitals();
        stockDataService.getAvailableTickers().forEach(ticker ->
                capitals.forEach(capital -> {
                    String url = String.format("%s/how-much-income/%d/%s", baseUrl, capital, ticker);
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

    // 금액 대역폭 생성 (촘촘하게)
    private List<Integer> generateAmounts() {
        List<Integer> amounts = new ArrayList<>();
        for (int i = 100; i <= 1000; i += 50) amounts.add(i);       // $100~$1000
        for (int i = 1100; i <= 5000; i += 100) amounts.add(i);     // $1100~$5000
        for (int i = 6000; i <= 20000; i += 1000) amounts.add(i);   // $6000~$20000
        return amounts;
    }

    // 자본금 대역폭 생성
    private List<Integer> generateCapitals() {
        List<Integer> capitals = new ArrayList<>();
        for (int i = 1000; i <= 10000; i += 1000) capitals.add(i);      // 1천~1만불
        for (int i = 15000; i <= 100000; i += 5000) capitals.add(i);    // 1.5만~10만불
        for (int i = 150000; i <= 1000000; i += 50000) capitals.add(i); // 15만~100만불
        return capitals;
    }
}