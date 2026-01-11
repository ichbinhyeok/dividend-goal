package org.example.dividendgoal.service;

import org.example.dividendgoal.model.Stock;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BestStockService {

    private final StockDataService stockDataService;

    public BestStockService(StockDataService stockDataService) {
        this.stockDataService = stockDataService;
    }

    /**
     * Finds the best "Safety" stocks (Low Risk, decent yield).
     * Criteria: Risk == LOW or MEDIUM, sort by Dividend Growth (5yr).
     */
    public List<Stock> getTopSafetyStocks(int limit) {
        return stockDataService.getAvailableTickers().stream()
                .map(stockDataService::findByTicker)
                .filter(opt -> opt.isPresent())
                .map(opt -> opt.get())
                .filter(s -> "LOW".equals(s.getRisk()) || "MEDIUM".equals(s.getRisk()))
                .sorted(Comparator.comparingDouble(Stock::getDividendGrowth).reversed()) // Growth is safety proxy
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Finds the best "Income" stocks (High Yield).
     * Criteria: Sort by Yield, but exclude High Risk if possible (or include for
     * Income focus).
     */
    public List<Stock> getTopIncomeStocks(int limit) {
        return stockDataService.getAvailableTickers().stream()
                .map(stockDataService::findByTicker)
                .filter(opt -> opt.isPresent())
                .map(opt -> opt.get())
                .sorted(Comparator.comparingDouble(Stock::getYield).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Returns a mixed portfolio suggestions.
     */
    public List<Stock> getBalancedPortfolio() {
        // Simple logic: Take 1 Safety + 1 Income + 1 Balanced (SCHD usually)
        return List.of(
                getTopSafetyStocks(1).get(0),
                getTopIncomeStocks(1).get(0));
    }
}
