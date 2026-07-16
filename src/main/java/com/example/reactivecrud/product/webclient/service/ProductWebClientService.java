package com.example.reactivecrud.product.webclient.service;

import com.example.reactivecrud.product.dto.ProductRequest;
import com.example.reactivecrud.product.dto.ProductResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductWebClientService {

    private final WebClient webClient;

    public ProductWebClientService(
            WebClient.Builder webClientBuilder,
            @Value("${app.self-base-url:http://localhost:8080}") String selfBaseUrl
    ) {
        this.webClient = webClientBuilder.baseUrl(selfBaseUrl).build();
    }

    public Flux<ProductResponse> findAll() {
        return webClient.get()
                .uri("/api/products")
                .retrieve()
                .bodyToFlux(ProductResponse.class);
    }

    public Mono<ProductResponse> findById(Long id) {
        return webClient.get()
                .uri("/api/products/{id}", id)
                .retrieve()
                .bodyToMono(ProductResponse.class);
    }

    public Mono<ProductResponse> create(ProductRequest request) {
        return webClient.post()
                .uri("/api/products")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ProductResponse.class);
    }
}
