package com.pretrade.learner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

import com.pretrade.learner.config.LearnerSettings;

@SpringBootApplication(scanBasePackages = {"com.pretrade.learner", "com.pretrade.utils"})
@EnableAsync
@EnableConfigurationProperties(LearnerSettings.class)
@EntityScan(basePackages = "com.pretrade.shared.models")
@EnableJpaRepositories(basePackages = "com.pretrade.learner.db")
public class LearningSummaryApplication {

    public static void main(String[] args) {
        SpringApplication.run(LearningSummaryApplication.class, args);
    }
}
