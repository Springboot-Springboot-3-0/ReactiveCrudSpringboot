package com.example.reactivecrud.product.performance.controller;

import com.example.reactivecrud.product.dto.ProductResponse;
import com.example.reactivecrud.product.performance.service.PerformanceClientService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/performance")
public class PerformanceController {

    private final PerformanceClientService performanceClientService;

    public PerformanceController(PerformanceClientService performanceClientService) {
        this.performanceClientService = performanceClientService;
    }

    @GetMapping("/ping")
    public Mono<Map<String, Object>> ping(@RequestParam(defaultValue = "2048") int size) {
        int safeSize = Math.max(512, size);
        String payload = "x".repeat(safeSize);
        return Mono.just(Map.of(
                "message", "high-performance-ready",
                "payloadSize", safeSize,
                "payload", payload
        ));
    }

    @GetMapping("/products")
    public Flux<ProductResponse> findAllProductsWithPooledClient() {
        return performanceClientService.findAllProducts();
    }
}
