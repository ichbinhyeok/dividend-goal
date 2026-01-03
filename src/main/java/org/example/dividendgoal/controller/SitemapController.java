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
import java.util.Arrays;
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

        // 헤더 생성
        xml.append("""
                <?xml version="1.0" encoding="UTF-8"?>
                <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
                """);

        // 1. 고정 페이지 (메인, 소개, 약관 등) - 우선순위 높음
        xml.append(buildUrl(baseUrl + "/", "1.0"));
        xml.append(buildUrl(baseUrl + "/articles", "0.9")); // 블로그 메인
        xml.append(buildUrl(baseUrl + "/about", "0.5"));
        xml.append(buildUrl(baseUrl + "/privacy-policy", "0.3"));
        xml.append(buildUrl(baseUrl + "/disclaimer", "0.3"));

        // 1-1. 블로그 글 (Articles) - 수동 추가 필요 (나중에 DB 연동 시 자동화)
        List<String> articles = Arrays.asList(
                "what-is-dividend-yield",
                "why-dividend-growth-matters",
                "schd-vs-jepi-comparison",
                "how-to-use-dividend-calculator",
                "dividend-income-vs-interest"
        );
        for (String slug : articles) {
            xml.append(buildUrl(baseUrl + "/articles/" + slug, "0.8"));
        }

        // 2. 월 배당 목표 계산기 URL (핵심 키워드만 생성)
        List<Integer> amounts = generateAmountsLite();
        stockDataService.getAvailableTickers().forEach(ticker ->
                amounts.forEach(amount -> {
                    String url = String.format("%s/how-much-dividend/%d-per-month/%s", baseUrl, amount, ticker);
                    xml.append(buildUrl(url, "0.6"));
                })
        );

        // 3. 투자금 역산 계산기 URL (핵심 키워드만 생성)
        List<Integer> capitals = generateCapitalsLite();
        stockDataService.getAvailableTickers().forEach(ticker ->
                capitals.forEach(capital -> {
                    String url = String.format("%s/how-much-income/%d/%s", baseUrl, capital, ticker);
                    xml.append(buildUrl(url, "0.6"));
                })
        );

        xml.append("</urlset>");
        return ResponseEntity.ok(xml.toString());
    }

    private String buildUrl(String location, String priority) {
        return String.format("""
                <url>
                    <loc>%s</loc>
                    <lastmod>%s</lastmod>
                    <priority>%s</priority>
                </url>
                """, location, LocalDate.now(), priority);
    }

    // [수정] 금액 대역폭 대폭 축소 (핵심 검색어 위주)
    private List<Integer> generateAmountsLite() {
        // 사람들이 검색할만한 "딱 떨어지는" 숫자만 남김
        return Arrays.asList(
                100, 300, 500, 1000,    // 생활비 보조
                1500, 2000, 3000, 5000, // 월급 대체
                10000                   // 경제적 자유
        );
    }

    // [수정] 자본금 대역폭 대폭 축소
    private List<Integer> generateCapitalsLite() {
        return Arrays.asList(
                10000, 50000,           // 시드머니 모으기
                100000, 300000, 500000, // 1억 ~ 5억
                1000000                 // 10억 (백만불)
        );
    }
}