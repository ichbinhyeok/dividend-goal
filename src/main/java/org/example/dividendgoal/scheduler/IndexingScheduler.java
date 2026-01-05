package org.example.dividendgoal.scheduler;

import org.example.dividendgoal.service.GoogleIndexingService;
import org.example.dividendgoal.service.LifestyleService;
import org.example.dividendgoal.service.StockDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class IndexingScheduler {

    private static final Logger logger = LoggerFactory.getLogger(IndexingScheduler.class);
    private final GoogleIndexingService indexingService;
    private final StockDataService stockDataService;
    private final LifestyleService lifestyleService;

    private int currentOffset = 0;
    private static final int BATCH_SIZE = 50;

    public IndexingScheduler(GoogleIndexingService indexingService, StockDataService stockDataService, LifestyleService lifestyleService) {
        this.indexingService = indexingService;
        this.stockDataService = stockDataService;
        this.lifestyleService = lifestyleService;
    }

    // 매일 새벽 4시 실행
    @Scheduled(cron = "0 0 4 * * *")
    public void submitBatchToGoogle() {
        logger.info("Starting daily indexing batch...");
        List<String> allUrls = generateAllUrls();

        if (currentOffset >= allUrls.size()) currentOffset = 0;
        int end = Math.min(currentOffset + BATCH_SIZE, allUrls.size());

        if (currentOffset < end) {
            List<String> batch = allUrls.subList(currentOffset, end);
            indexingService.publishBatch(batch); // 실제 요청
            logger.info("Submitted {} URLs (Offset: {} -> {})", batch.size(), currentOffset, end);
            currentOffset = end;
        }
    }

    private List<String> generateAllUrls() {
        List<String> urls = new ArrayList<>();
        String baseUrl = "https://www.dividend-goal.com"; // 실제 도메인으로 변경 필수
        lifestyleService.getAllItems().forEach(item -> {
            stockDataService.getAvailableTickers().forEach(ticker -> {
                urls.add(String.format("%s/lifestyle/cost-of-%s-vs-%s-dividend", baseUrl, item.getSlug(), ticker));
            });
        });
        return urls;
    }
}