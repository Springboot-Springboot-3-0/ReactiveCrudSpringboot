package com.example.reactivecrud.product.webclient;

import com.example.reactivecrud.product.dto.ProductRequest;
import com.example.reactivecrud.product.dto.ProductResponse;
import java.io.IOException;
import java.math.BigDecimal;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

class ProductWebClientServiceTest {

    private MockWebServer server;
    private ProductWebClientService service;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        service = new ProductWebClientService(WebClient.builder(), server.url("/").toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void findAllShouldCallProductsEndpoint() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("[{\"id\":1,\"name\":\"Laptop\",\"description\":\"Lightweight\",\"price\":999.99}]"));

        StepVerifier.create(service.findAll())
                .assertNext(response -> {
                    Assertions.assertEquals(1L, response.id());
                    Assertions.assertEquals("Laptop", response.name());
                })
                .verifyComplete();

        try {
            var request = server.takeRequest();
            Assertions.assertEquals("GET", request.getMethod());
            Assertions.assertEquals("/api/products", request.getPath());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            Assertions.fail("Interrupted while reading request", ex);
        }
    }

    @Test
    void findByIdShouldCallProductsByIdEndpoint() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"id\":5,\"name\":\"Phone\",\"description\":\"Updated\",\"price\":599.99}"));

        StepVerifier.create(service.findById(5L))
                .assertNext(response -> {
                    Assertions.assertEquals(5L, response.id());
                    Assertions.assertEquals("Phone", response.name());
                })
                .verifyComplete();

        try {
            var request = server.takeRequest();
            Assertions.assertEquals("GET", request.getMethod());
            Assertions.assertEquals("/api/products/5", request.getPath());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            Assertions.fail("Interrupted while reading request", ex);
        }
    }

    @Test
    void createShouldPostToProductsEndpoint() {
        ProductRequest request = new ProductRequest("Keyboard", "Mechanical", new BigDecimal("79.99"));
        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"id\":10,\"name\":\"Keyboard\",\"description\":\"Mechanical\",\"price\":79.99}"));

        StepVerifier.create(service.create(request))
                .assertNext(response -> {
                    Assertions.assertEquals(10L, response.id());
                    Assertions.assertEquals("Keyboard", response.name());
                })
                .verifyComplete();

        try {
            var recorded = server.takeRequest();
            Assertions.assertEquals("POST", recorded.getMethod());
            Assertions.assertEquals("/api/products", recorded.getPath());
            Assertions.assertTrue(recorded.getBody().readUtf8().contains("Keyboard"));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            Assertions.fail("Interrupted while reading request", ex);
        }
    }
}
