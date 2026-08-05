package com.example.reactivecrud.product.controller;

import com.example.reactivecrud.product.dto.ProductRequest;
import com.example.reactivecrud.product.dto.ProductResponse;
import com.example.reactivecrud.product.service.ProductService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Test
    void findAllShouldDelegateToService() {
        ProductController controller = new ProductController(productService);
        when(productService.findAll())
                .thenReturn(Flux.just(new ProductResponse(1L, "Laptop", "Lightweight", new BigDecimal("999.99"))));

        StepVerifier.create(controller.findAll())
                .assertNext(response -> Assertions.assertEquals("Laptop", response.getName()))
                .verifyComplete();

        verify(productService).findAll();
    }

    @Test
    void findByIdShouldDelegateToService() {
        ProductController controller = new ProductController(productService);
        when(productService.findById(4L))
                .thenReturn(Mono.just(new ProductResponse(4L, "Phone", "Flagship", new BigDecimal("599.99"))));

        StepVerifier.create(controller.findById(4L))
                .assertNext(response -> Assertions.assertEquals(4L, response.getId()))
                .verifyComplete();

        verify(productService).findById(4L);
    }

    @Test
    void createShouldReturn201WithLocation() {
        ProductController controller = new ProductController(productService);
        ProductRequest request = new ProductRequest("Keyboard", "Mechanical", new BigDecimal("79.99"));
        when(productService.create(request))
                .thenReturn(Mono.just(new ProductResponse(8L, "Keyboard", "Mechanical", new BigDecimal("79.99"))));

        StepVerifier.create(controller.create(request))
                .assertNext(entity -> {
                    Assertions.assertEquals(HttpStatus.CREATED, entity.getStatusCode());
                    Assertions.assertEquals("/api/products/8", entity.getHeaders().getLocation().toString());
                    Assertions.assertEquals(8L, entity.getBody().getId());
                })
                .verifyComplete();
    }

    @Test
    void deleteShouldReturn204() {
        ProductController controller = new ProductController(productService);
        when(productService.delete(3L)).thenReturn(Mono.empty());

        StepVerifier.create(controller.delete(3L))
                .assertNext(entity -> Assertions.assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode()))
                .verifyComplete();

        verify(productService).delete(3L);
    }
}
