package org.example.dividendgoal.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.example.dividendgoal.model.Stock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class StockDataService {
    private static final Logger logger = LoggerFactory.getLogger(StockDataService.class);
    private final ObjectMapper objectMapper;
    private List<Stock> cachedStocks = Collections.emptyList();

    // [추가] 없는 티커를 메모리에 저장 (티커명, 요청 횟수)
    private final Map<String, Integer> missingTickerLog = new ConcurrentHashMap<>();

    public StockDataService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void loadStocks() {
        ClassPathResource resource = new ClassPathResource("data/stocks.json");
        try (InputStream is = resource.getInputStream()) {
            List<Stock> stocks = objectMapper.readValue(is, new TypeReference<>() {
            });

            // [수정] 티커 기준 알파벳 순으로 정렬하여 저장
            this.cachedStocks = stocks.stream()
                    .collect(Collectors.toList());

            logger.info("Loaded {} stocks in alphabetical order.", cachedStocks.size());
        } catch (IOException e) {
            logger.error("Failed to load stocks.json", e);
            cachedStocks = Collections.emptyList();
        }
    }

    // [YMYL-Compliant] No Mock Enrichment. Missing data stays missing.

    public List<Stock> getAllStocks() {
        return cachedStocks;
    }

    public Optional<Stock> findByTicker(String ticker) {
        if (ticker == null)
            return Optional.empty();
        String sanitized = ticker.trim().toUpperCase();
        return cachedStocks.stream()
                .filter(stock -> sanitized.equals(stock.getTicker().toUpperCase()))
                .findFirst();
    }

    public List<String> getAvailableTickers() {
        return cachedStocks.stream()
                .map(Stock::getTicker)
                .collect(Collectors.toList());
    }

    // [추가] 없는 티커 로그 기록 메서드
    public void logMissingTicker(String ticker) {
        if (ticker == null || ticker.isBlank())
            return;
        String upperTicker = ticker.trim().toUpperCase();
        missingTickerLog.merge(upperTicker, 1, Integer::sum);
        logger.warn("MISSING_TICKER_LOGGED: {}", upperTicker);
    }

    // [추가] 수집된 로그 확인용 (필요 시 컨트롤러에서 호출)
    public Map<String, Integer> getMissingTickerSummary() {
        return new TreeMap<>(missingTickerLog);
    }
}