package com.pretrade.executor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.pretrade.executor.config.ExecutorSettings;

@SpringBootApplication(scanBasePackages = {"com.pretrade.executor", "com.pretrade.utils"})
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(ExecutorSettings.class)
@EntityScan(basePackages = "com.pretrade.shared.models")
@EnableJpaRepositories(basePackages = "com.pretrade.executor.db")
public class TradeExecutorApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradeExecutorApplication.class, args);
    }
}
