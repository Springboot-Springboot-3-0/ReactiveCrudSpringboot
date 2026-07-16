package com.example.reactivecrud.product;

import com.example.reactivecrud.product.dto.ProductRequest;
import com.example.reactivecrud.product.exception.ProductNotFoundException;
import com.example.reactivecrud.product.model.Product;
import com.example.reactivecrud.product.repository.ProductRepository;
import com.example.reactivecrud.product.service.ProductService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository);
    }

    @Test
    void createShouldSaveAndReturnProduct() {
        ProductRequest request = new ProductRequest("Keyboard", "Mechanical keyboard", new BigDecimal("99.99"));
        when(productRepository.save(any(Product.class)))
                .thenReturn(Mono.just(new Product(1L, request.name(), request.description(), request.price())));

        StepVerifier.create(productService.create(request))
                .assertNext(response -> {
                    Assertions.assertEquals(1L, response.id());
                    Assertions.assertEquals("Keyboard", response.name());
                })
                .verifyComplete();
    }

    @Test
    void findByIdShouldReturnNotFoundWhenMissing() {
        when(productRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(productService.findById(99L))
                .expectErrorSatisfies(error -> Assertions.assertInstanceOf(ProductNotFoundException.class, error))
                .verify();
    }

    @Test
    void updateShouldKeepExistingId() {
        Product existing = new Product(5L, "Old", "Old description", new BigDecimal("10.00"));
        ProductRequest request = new ProductRequest("New", "New description", new BigDecimal("12.50"));

        when(productRepository.findById(5L)).thenReturn(Mono.just(existing));
        when(productRepository.save(any(Product.class)))
                .thenReturn(Mono.just(new Product(5L, request.name(), request.description(), request.price())));

        StepVerifier.create(productService.update(5L, request))
                .assertNext(response -> {
                    Assertions.assertEquals(5L, response.id());
                    Assertions.assertEquals("New", response.name());
                })
                .verifyComplete();
    }

    @Test
    void deleteShouldFailWhenProductDoesNotExist() {
        when(productRepository.findById(10L)).thenReturn(Mono.empty());

        StepVerifier.create(productService.delete(10L))
                .expectError(ProductNotFoundException.class)
                .verify();
    }

    @Test
    void deleteShouldRemoveExistingProduct() {
        Product existing = new Product(10L, "Mouse", "Wireless mouse", new BigDecimal("25.00"));
        when(productRepository.findById(10L)).thenReturn(Mono.just(existing));
        when(productRepository.delete(existing)).thenReturn(Mono.empty());

        StepVerifier.create(productService.delete(10L)).verifyComplete();

        verify(productRepository).delete(existing);
    }

    @Test
    void searchByNameShouldReturnMatchingProducts() {
    when(productRepository.findByNameContainingIgnoreCase("lap"))
        .thenReturn(reactor.core.publisher.Flux.just(
            new Product(1L, "Laptop", "Lightweight", new BigDecimal("999.99"))
        ));

    StepVerifier.create(productService.searchByName("lap"))
        .assertNext(response -> Assertions.assertEquals("Laptop", response.name()))
        .verifyComplete();
    }

    @Test
    void findByPriceRangeShouldReturnProductsInRange() {
    when(productRepository.findByPriceBetween(new BigDecimal("100.00"), new BigDecimal("300.00")))
        .thenReturn(reactor.core.publisher.Flux.just(
            new Product(2L, "Headphones", "Noise cancelling", new BigDecimal("199.99"))
        ));

    StepVerifier.create(productService.findByPriceRange(new BigDecimal("100.00"), new BigDecimal("300.00")))
        .assertNext(response -> Assertions.assertEquals("Headphones", response.name()))
        .verifyComplete();
    }
}
