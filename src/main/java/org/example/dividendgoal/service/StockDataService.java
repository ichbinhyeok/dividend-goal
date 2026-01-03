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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StockDataService {
    private static final Logger logger = LoggerFactory.getLogger(StockDataService.class);
    private final ObjectMapper objectMapper;
    private List<Stock> cachedStocks = Collections.emptyList();

    public StockDataService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void loadStocks() {
        ClassPathResource resource = new ClassPathResource("data/stocks.json");
        try (InputStream is = resource.getInputStream()) {
            cachedStocks = objectMapper.readValue(is, new TypeReference<>() {});
        } catch (IOException e) {
            logger.error("Failed to load stocks.json", e);
            cachedStocks = Collections.emptyList();
        }
    }

    public List<Stock> getAllStocks() {
        return cachedStocks;
    }

    public Optional<Stock> findByTicker(String ticker) {
        if (ticker == null) {
            return Optional.empty();
        }
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
}