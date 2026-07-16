package com.example.reactivecrud.product.streaming.service;

import com.example.reactivecrud.product.model.Product;
import com.example.reactivecrud.product.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.Duration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductStreamingServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Test
    void streamProductsShouldEmitRepositoryDataOnEachTick() {
        when(productRepository.findAll())
                .thenReturn(Flux.just(new Product(1L, "Laptop", "Lightweight", new BigDecimal("999.99"))));

        ProductStreamingService service = new ProductStreamingService(productRepository);

        StepVerifier.withVirtualTime(() -> service.streamProducts(Duration.ofSeconds(1)).take(1))
                .thenAwait(Duration.ofSeconds(1))
                .assertNext(response -> {
                    Assertions.assertEquals(1L, response.id());
                    Assertions.assertEquals("Laptop", response.name());
                })
                .verifyComplete();

        verify(productRepository, atLeastOnce()).findAll();
    }
}
