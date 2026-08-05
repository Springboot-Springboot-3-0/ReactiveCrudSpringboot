package com.example.reactivecrud.product.controller;

import com.example.reactivecrud.product.dto.ProductRequest;
import com.example.reactivecrud.product.dto.ProductResponse;
import com.example.reactivecrud.product.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/products")
@Validated
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public Flux<ProductResponse> findAll() {
        return productService.findAll();
    }

    @GetMapping("/{id}")
    public Mono<ProductResponse> findById(@PathVariable @Positive(message = "id must be greater than zero") Long id) {
        return productService.findById(id);
    }

    @PostMapping
    public Mono<ResponseEntity<ProductResponse>> create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request)
                // 201 Created + a Location header pointing at the new product (/api/products/{id}),
                // with the saved product as the response body.
                .map(response -> ResponseEntity
                        .created(URI.create("/api/products/" + response.getId()))
                        .body(response));
    }

    @PutMapping("/{id}")
    public Mono<ProductResponse> update(
            @PathVariable @Positive(message = "id must be greater than zero") Long id,
            @Valid @RequestBody ProductRequest request
    ) {
        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable @Positive(message = "id must be greater than zero") Long id) {
        return productService.delete(id)
                .thenReturn(ResponseEntity.noContent().build());
    }
}
