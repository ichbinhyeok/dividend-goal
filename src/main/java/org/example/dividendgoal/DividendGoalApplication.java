package org.example.dividendgoal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching // [Performance] Enable server-side caching
public class DividendGoalApplication {

    public static void main(String[] args) {
        SpringApplication.run(DividendGoalApplication.class, args);
    }

    @org.springframework.context.annotation.Bean
    public org.springframework.boot.CommandLineRunner logPort() {
        return args -> {
            String port = System.getenv("PORT");
            System.out.println("DEBUG: PORT environment variable is: " + (port != null ? port : "NULL"));
        };
    }

}
