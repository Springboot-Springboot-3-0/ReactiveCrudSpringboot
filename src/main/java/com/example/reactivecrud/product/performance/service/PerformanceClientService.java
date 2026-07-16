package com.example.reactivecrud.product.performance.service;

import com.example.reactivecrud.product.dto.ProductResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Service
public class PerformanceClientService {

    private final WebClient webClient;

    public PerformanceClientService(
            WebClient.Builder webClientBuilder,
            @Value("${app.self-base-url:http://localhost:8080}") String selfBaseUrl
    ) {
        this.webClient = webClientBuilder.baseUrl(selfBaseUrl).build();
    }

    public Flux<ProductResponse> findAllProducts() {
        return webClient.get()
                .uri("/api/products")
                .retrieve()
                .bodyToFlux(ProductResponse.class);
    }
}
