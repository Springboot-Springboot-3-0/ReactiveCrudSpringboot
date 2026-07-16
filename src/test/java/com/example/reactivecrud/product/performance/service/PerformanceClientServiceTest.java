package com.example.reactivecrud.product.performance.service;

import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

class PerformanceClientServiceTest {

    private MockWebServer server;
    private PerformanceClientService service;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        service = new PerformanceClientService(WebClient.builder(), server.url("/").toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void findAllProductsShouldCallProductsEndpoint() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("[{\"id\":1,\"name\":\"Laptop\",\"description\":\"Lightweight\",\"price\":999.99}]"));

        StepVerifier.create(service.findAllProducts())
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
}
