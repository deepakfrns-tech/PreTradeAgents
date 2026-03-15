package com.pretrade.learner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LearningSummaryApplicationTest {

    @Test
    void applicationClass_exists() {
        assertNotNull(LearningSummaryApplication.class);
    }

    @Test
    void applicationClass_hasMainMethod() throws NoSuchMethodException {
        assertNotNull(LearningSummaryApplication.class.getMethod("main", String[].class));
    }

    @Test
    void applicationClass_hasSpringBootAnnotation() {
        assertTrue(LearningSummaryApplication.class.isAnnotationPresent(
                org.springframework.boot.autoconfigure.SpringBootApplication.class));
    }

    @Test
    void applicationClass_hasAsyncEnabled() {
        assertTrue(LearningSummaryApplication.class.isAnnotationPresent(
                org.springframework.scheduling.annotation.EnableAsync.class));
    }
}
