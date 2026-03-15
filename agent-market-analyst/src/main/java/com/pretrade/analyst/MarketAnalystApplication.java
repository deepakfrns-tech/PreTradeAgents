package com.pretrade.analyst;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

import com.pretrade.analyst.config.AnalystSettings;

@SpringBootApplication(scanBasePackages = {"com.pretrade.analyst", "com.pretrade.utils"})
@EnableAsync
@EnableConfigurationProperties(AnalystSettings.class)
@EntityScan(basePackages = "com.pretrade.shared.models")
@EnableJpaRepositories(basePackages = "com.pretrade.analyst.db")
public class MarketAnalystApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketAnalystApplication.class, args);
    }
}
