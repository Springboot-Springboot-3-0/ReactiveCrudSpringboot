package com.example.reactivecrud.product.service;

import com.example.reactivecrud.product.dto.ProductRequest;
import com.example.reactivecrud.product.dto.ProductResponse;
import com.example.reactivecrud.product.exception.ProductNotFoundException;
import com.example.reactivecrud.product.model.Product;
import com.example.reactivecrud.product.repository.ProductRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Flux<ProductResponse> findAll() {
        return productRepository.findAll().map(ProductResponse::from);
    }

    public Flux<ProductResponse> searchByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name).map(ProductResponse::from);
    }

    public Flux<ProductResponse> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice).map(ProductResponse::from);
    }

    public Mono<ProductResponse> findById(Long id) {
        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(id)))
                .map(ProductResponse::from);
    }

    public Mono<ProductResponse> create(ProductRequest request) {
        Product product = toProduct(null, request);
        return productRepository.save(product).map(ProductResponse::from);
    }

    public Mono<ProductResponse> update(Long id, ProductRequest request) {
        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(id)))
                .map(existing -> toProduct(existing.getId(), request))
                .flatMap(productRepository::save)
                .map(ProductResponse::from);
    }

    public Mono<Void> delete(Long id) {
        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(id)))
                .flatMap(productRepository::delete);
    }

    private Product toProduct(Long id, ProductRequest request) {
        return new Product(id, request.name().trim(), request.description(), request.price());
    }
}
