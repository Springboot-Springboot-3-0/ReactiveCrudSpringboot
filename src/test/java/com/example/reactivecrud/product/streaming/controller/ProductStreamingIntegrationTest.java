package com.example.reactivecrud.product.streaming.controller;

import com.example.reactivecrud.product.dto.ProductResponse;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

@SpringBootTest
class ProductStreamingIntegrationTest {

    private WebTestClient webTestClient;

    @Autowired
    private DatabaseClient databaseClient;

    @Autowired
    private ApplicationContext applicationContext;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext).configureClient().build();
        databaseClient.sql("TRUNCATE TABLE products RESTART IDENTITY").fetch().rowsUpdated().block();
    }

    @Test
    void streamEndpointShouldEmitProductData() {
        databaseClient.sql("INSERT INTO products(name, description, price) VALUES('Stream Laptop', 'Streaming demo', 999.99)")
                .fetch()
                .rowsUpdated()
                .block();

        FluxExchangeResult<ProductResponse> result = webTestClient.get()
                .uri("/api/products/stream?intervalSeconds=1")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(ProductResponse.class);

        StepVerifier.create(result.getResponseBody().take(1))
                .assertNext(response -> {
                    org.junit.jupiter.api.Assertions.assertEquals("Stream Laptop", response.name());
                })
                .expectComplete()
                .verify(Duration.ofSeconds(3));
    }
}
