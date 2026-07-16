package com.example.reactivecrud.product.streaming.service;

import com.example.reactivecrud.product.dto.ProductResponse;
import com.example.reactivecrud.product.repository.ProductRepository;
import java.time.Duration;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ProductStreamingService {

    private final ProductRepository productRepository;

    public ProductStreamingService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Flux<ProductResponse> streamProducts(Duration interval) {
        return Flux.interval(interval)
                .onBackpressureDrop()
                .flatMap(tick -> productRepository.findAll().map(ProductResponse::from));
    }
}
