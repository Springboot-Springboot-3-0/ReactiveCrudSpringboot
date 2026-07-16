package com.example.reactivecrud.product.performance.controller;

import com.example.reactivecrud.product.dto.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    properties = {"server.port=8080", "app.self-base-url=http://localhost:8080"}
)
class PerformanceControllerIntegrationTest {

    private WebTestClient webTestClient;

    @Autowired
    private DatabaseClient databaseClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:8080").build();
        databaseClient.sql("TRUNCATE TABLE products RESTART IDENTITY").fetch().rowsUpdated().block();
    }

    @Test
    void pingEndpointShouldReturnLargePayload() {
        webTestClient.get()
                .uri("/api/performance/ping?size=2048")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("high-performance-ready")
                .jsonPath("$.payloadSize").isEqualTo(2048);
    }

    @Test
    void pooledClientEndpointShouldReturnProducts() {
        databaseClient.sql("INSERT INTO products(name, description, price) VALUES('Pool Test', 'Connection pool check', 123.45)")
                .fetch()
                .rowsUpdated()
                .block();

        webTestClient.get()
                .uri("/api/performance/products")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponse.class)
                .hasSize(1)
                .value(list -> org.junit.jupiter.api.Assertions.assertEquals("Pool Test", list.getFirst().name()));
    }
}
