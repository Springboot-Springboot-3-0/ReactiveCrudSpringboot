package com.example.reactivecrud.common.filter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
class RequestTracingWebFilterIntegrationTest {

    private WebTestClient webTestClient;

    @Autowired
    private ApplicationContext applicationContext;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext).configureClient().build();
    }

    @Test
    void shouldReuseIncomingRequestId() {
        webTestClient.get()
                .uri("/api/filter/ping")
                .header("X-Request-Id", "trace-123")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Request-Id", "trace-123")
                .expectHeader().value("X-Response-Time-Ms", value ->
                    Assertions.assertTrue(Long.parseLong(value.split(",")[0].trim()) >= 0));
    }

    @Test
    void shouldGenerateRequestIdWhenMissing() {
        webTestClient.get()
                .uri("/api/filter/ping")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().value("X-Request-Id", value ->
                        Assertions.assertFalse(value.isBlank()))
                .expectHeader().value("X-Response-Time-Ms", value ->
                    Assertions.assertTrue(Long.parseLong(value.split(",")[0].trim()) >= 0));
    }
}
