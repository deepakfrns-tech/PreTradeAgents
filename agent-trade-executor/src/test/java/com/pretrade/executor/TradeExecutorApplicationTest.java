package com.pretrade.executor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TradeExecutorApplicationTest {

    @Test
    void applicationClass_exists() {
        // Verify the application class can be loaded
        assertNotNull(TradeExecutorApplication.class);
    }

    @Test
    void applicationClass_hasMainMethod() throws NoSuchMethodException {
        // Verify main method exists with correct signature
        assertNotNull(TradeExecutorApplication.class.getMethod("main", String[].class));
    }

    @Test
    void applicationClass_hasSpringBootAnnotation() {
        assertTrue(TradeExecutorApplication.class.isAnnotationPresent(
                org.springframework.boot.autoconfigure.SpringBootApplication.class));
    }

    @Test
    void applicationClass_hasAsyncEnabled() {
        assertTrue(TradeExecutorApplication.class.isAnnotationPresent(
                org.springframework.scheduling.annotation.EnableAsync.class));
    }

    @Test
    void applicationClass_hasSchedulingEnabled() {
        assertTrue(TradeExecutorApplication.class.isAnnotationPresent(
                org.springframework.scheduling.annotation.EnableScheduling.class));
    }
}
