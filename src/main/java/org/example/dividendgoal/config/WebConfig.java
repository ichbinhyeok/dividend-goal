package org.example.dividendgoal.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

/**
 * [Performance] Web configuration for static resource caching
 * 
 * Static resources (CSS, JS, images, favicon) are cached for 30 days in
 * browser.
 * This significantly reduces repeat visitor load times and server bandwidth.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Static resources: 30 days browser caching
        registry.addResourceHandler("/static/**", "/favicon.png")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic());
    }
}
