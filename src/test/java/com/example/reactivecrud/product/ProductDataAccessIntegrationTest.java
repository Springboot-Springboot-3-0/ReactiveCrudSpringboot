package com.example.reactivecrud.product;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
class ProductDataAccessIntegrationTest {

    private WebTestClient webTestClient;

    @Autowired
    private DatabaseClient databaseClient;

    @Autowired
    private ApplicationContext applicationContext;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext).configureClient().build();
        databaseClient.sql("TRUNCATE TABLE products RESTART IDENTITY").fetch().rowsUpdated().block();

        databaseClient.sql("INSERT INTO products(name, description, price) VALUES('Laptop Pro', 'High end', 1200.00)")
                .fetch().rowsUpdated().block();
        databaseClient.sql("INSERT INTO products(name, description, price) VALUES('Laptop Air', 'Portable', 900.00)")
                .fetch().rowsUpdated().block();
        databaseClient.sql("INSERT INTO products(name, description, price) VALUES('Mouse', 'Wireless', 50.00)")
                .fetch().rowsUpdated().block();
    }

    @Test
    void searchEndpointShouldReturnMatchingNames() {
        webTestClient.get()
                .uri("/api/products/search?name=laptop")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(com.example.reactivecrud.product.dto.ProductResponse.class)
                .hasSize(2)
                .value(list -> org.junit.jupiter.api.Assertions.assertTrue(
                        list.stream().allMatch(p -> p.name().toLowerCase().contains("laptop"))
                ));
    }

    @Test
    void filterEndpointShouldReturnProductsWithinPriceRange() {
        webTestClient.get()
                .uri("/api/products/filter?minPrice=800&maxPrice=1300")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(com.example.reactivecrud.product.dto.ProductResponse.class)
                .hasSize(2)
                .value(list -> org.junit.jupiter.api.Assertions.assertTrue(
                        list.stream().allMatch(p -> p.price().compareTo(new BigDecimal("800")) >= 0
                                && p.price().compareTo(new BigDecimal("1300")) <= 0)
                ));
    }
}
