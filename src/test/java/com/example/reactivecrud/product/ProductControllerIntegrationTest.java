package com.example.reactivecrud.product;

import com.example.reactivecrud.product.dto.ProductRequest;
import com.example.reactivecrud.product.dto.ProductResponse;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
class ProductControllerIntegrationTest {

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
    void shouldCreateAndFetchProducts() {
        ProductRequest request = new ProductRequest("Laptop", "Lightweight laptop", new BigDecimal("999.99"));

        ProductResponse created = webTestClient.post()
                .uri("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueEquals("Location", "/api/products/1")
                .expectBody(ProductResponse.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertNotNull(created);
        Assertions.assertEquals(1L, created.getId());
        Assertions.assertEquals("Laptop", created.getName());

        webTestClient.get()
                .uri("/api/products/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Laptop")
                .jsonPath("$.price").isEqualTo(999.99);

        webTestClient.get()
                .uri("/api/products")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponse.class)
                .hasSize(1)
                .contains(created);
    }

    @Test
    void shouldUpdateAndDeleteProduct() {
        databaseClient.sql("INSERT INTO products(name, description, price) VALUES('Phone', 'Original', 499.99)")
                .fetch()
                .rowsUpdated()
                .block();

        ProductRequest updateRequest = new ProductRequest("Phone Pro", "Updated", new BigDecimal("599.99"));

        webTestClient.put()
                .uri("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.name").isEqualTo("Phone Pro")
                .jsonPath("$.description").isEqualTo("Updated")
                .jsonPath("$.price").isEqualTo(599.99);

        webTestClient.delete()
                .uri("/api/products/1")
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get()
                .uri("/api/products/1")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Resource not found");
    }

    @Test
    void shouldReturnValidationErrorsForBadInput() {
        ProductRequest invalidRequest = new ProductRequest(" ", "x".repeat(256), BigDecimal.ZERO);

        webTestClient.post()
                .uri("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Validation failed")
                .jsonPath("$.detail").isEqualTo("Request validation failed")
                .jsonPath("$.errors.length()").isEqualTo(3)
                .jsonPath("$.errors").value(errors -> {
                    @SuppressWarnings("unchecked")
                    List<String> messages = (List<String>) errors;
                    Assertions.assertTrue(messages.stream().anyMatch(message -> message.contains("name")));
                    Assertions.assertTrue(messages.stream().anyMatch(message -> message.contains("description")));
                    Assertions.assertTrue(messages.stream().anyMatch(message -> message.contains("price")));
                });
    }

    @Test
    void shouldReturnNotFoundForMissingProduct() {
        webTestClient.get()
                .uri("/api/products/99")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Resource not found")
                .jsonPath("$.detail").isEqualTo("Product 99 was not found");
    }

    @Test
    void shouldReturnValidationErrorWhenPriceMissing() {
        String payload = """
                {
                  "name": "Phone",
                  "description": "Missing price"
                }
                """;

        webTestClient.post()
                .uri("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Validation failed")
                .jsonPath("$.detail").isEqualTo("Request validation failed")
                .jsonPath("$.errors.length()").isEqualTo(1)
                .jsonPath("$.errors[0]").value(message ->
                        Assertions.assertTrue(message.toString().contains("price")));
    }

    @Test
    void shouldReturnValidationErrorWhenIdIsNotPositive() {
        webTestClient.get()
                .uri("/api/products/0")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Validation failed")
                .jsonPath("$.detail").isEqualTo("Request validation failed")
                .jsonPath("$.errors.length()").isEqualTo(1)
                .jsonPath("$.errors[0]").value(message ->
                        Assertions.assertTrue(message.toString().contains("id must be greater than zero")));
    }
}
