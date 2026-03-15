package com.pretrade.dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.pretrade.dashboard", "com.pretrade.utils"})
@EntityScan(basePackages = "com.pretrade.shared.models")
@EnableJpaRepositories(basePackages = "com.pretrade.dashboard.db")
public class TradeDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradeDashboardApplication.class, args);
    }
}
