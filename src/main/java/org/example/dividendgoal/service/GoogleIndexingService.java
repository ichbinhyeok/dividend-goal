package org.example.dividendgoal.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class GoogleIndexingService {
    private static final Logger logger = LoggerFactory.getLogger(GoogleIndexingService.class);

    public void publishBatch(List<String> urls) {
        // TODO: Google Indexing API 호출 로직 구현 (http client 사용)
        logger.info("[Mock] Publishing {} URLs to Google Indexing API", urls.size());
        urls.forEach(url -> logger.debug(" - URL: {}", url));
    }
}