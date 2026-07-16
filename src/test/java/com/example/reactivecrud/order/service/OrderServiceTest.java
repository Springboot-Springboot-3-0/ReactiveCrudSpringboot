package com.example.reactivecrud.order.service;

import com.example.reactivecrud.order.dto.OrderRequest;
import com.example.reactivecrud.order.exception.OrderNotFoundException;
import com.example.reactivecrud.order.model.Order;
import com.example.reactivecrud.order.repository.OrderRepository;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    private MockWebServer server;
    private OrderService orderService;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        orderService = new OrderService(orderRepository, server.url("/").toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void createShouldSaveOrder() {
        OrderRequest request = new OrderRequest(2L, 3);
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(new Order(1L, 2L, 3)));

        StepVerifier.create(orderService.create(request))
                .assertNext(response -> {
                    Assertions.assertEquals(1L, response.id());
                    Assertions.assertEquals(2L, response.productId());
                })
                .verifyComplete();
    }

    @Test
    void findByIdShouldReturnErrorWhenMissing() {
        when(orderRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(orderService.findById(99L))
                .expectError(OrderNotFoundException.class)
                .verify();
    }

    @Test
    void findDetailsByIdShouldCombineOrderAndProduct() {
        when(orderRepository.findById(1L)).thenReturn(Mono.just(new Order(1L, 5L, 2)));
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"id\":5,\"name\":\"Laptop\",\"description\":\"Lightweight\",\"price\":999.99}"));

        StepVerifier.create(orderService.findDetailsById(1L))
                .assertNext(details -> {
                    Assertions.assertEquals(1L, details.orderId());
                    Assertions.assertEquals(2, details.quantity());
                    Assertions.assertEquals("Laptop", details.product().name());
                })
                .verifyComplete();
    }
}
