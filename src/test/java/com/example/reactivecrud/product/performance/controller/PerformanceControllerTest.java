package com.example.reactivecrud.product.performance.controller;

import com.example.reactivecrud.product.dto.ProductResponse;
import com.example.reactivecrud.product.performance.service.PerformanceClientService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PerformanceControllerTest {

    @Mock
    private PerformanceClientService performanceClientService;

    @Test
    void pingShouldCreateLargePayload() {
        PerformanceController controller = new PerformanceController(performanceClientService);

        StepVerifier.create(controller.ping(2048))
                .assertNext(response -> {
                    Assertions.assertEquals("high-performance-ready", response.get("message"));
                    Assertions.assertEquals(2048, response.get("payloadSize"));
                    Assertions.assertEquals(2048, ((String) response.get("payload")).length());
                })
                .verifyComplete();
    }

    @Test
    void findAllProductsShouldDelegateToService() {
        PerformanceController controller = new PerformanceController(performanceClientService);
        when(performanceClientService.findAllProducts())
                .thenReturn(Flux.just(new ProductResponse(1L, "Laptop", "Lightweight", new BigDecimal("999.99"))));

        StepVerifier.create(controller.findAllProductsWithPooledClient())
                .assertNext(response -> Assertions.assertEquals("Laptop", response.name()))
                .verifyComplete();

        verify(performanceClientService).findAllProducts();
    }
}
