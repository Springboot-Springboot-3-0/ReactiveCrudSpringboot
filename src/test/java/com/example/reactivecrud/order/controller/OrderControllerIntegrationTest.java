package com.example.reactivecrud.order.controller;

import com.example.reactivecrud.order.dto.OrderRequest;
import com.example.reactivecrud.order.dto.OrderResponse;
import com.example.reactivecrud.product.dto.ProductRequest;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {"server.port=8080", "app.self-base-url=http://localhost:8080"}
)
class OrderControllerIntegrationTest {

    private WebTestClient webTestClient;

    @Autowired
    private DatabaseClient databaseClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:8080").build();
        databaseClient.sql("TRUNCATE TABLE products RESTART IDENTITY").fetch().rowsUpdated().block();
    }

    @Test
    void shouldCreateOrderAndFetchDetails() {
        ProductRequest productRequest = new ProductRequest("E2E Laptop", "E2E product", new BigDecimal("1200.00"));

        webTestClient.post()
                .uri("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(productRequest)
                .exchange()
                .expectStatus().isCreated();

        OrderRequest orderRequest = new OrderRequest(1L, 2);

        OrderResponse createdOrder = webTestClient.post()
                .uri("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(orderRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrderResponse.class)
                .returnResult()
                .getResponseBody();

        org.junit.jupiter.api.Assertions.assertNotNull(createdOrder);

        webTestClient.get()
                .uri("/api/orders/1/details")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.orderId").isEqualTo(1)
                .jsonPath("$.quantity").isEqualTo(2)
                .jsonPath("$.product.name").isEqualTo("E2E Laptop")
                .jsonPath("$.totalPrice").isEqualTo(2400.00);
    }

    @Test
    void shouldReturnNotFoundForMissingOrder() {
        webTestClient.get()
                .uri("/api/orders/999")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Resource not found")
                .jsonPath("$.detail").isEqualTo("Order 999 was not found");
    }
}
