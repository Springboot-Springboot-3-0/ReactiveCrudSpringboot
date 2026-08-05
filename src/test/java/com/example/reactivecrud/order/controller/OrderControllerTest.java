package com.example.reactivecrud.order.controller;

import com.example.reactivecrud.order.dto.OrderDetailsResponse;
import com.example.reactivecrud.order.dto.OrderRequest;
import com.example.reactivecrud.order.dto.OrderResponse;
import com.example.reactivecrud.order.service.OrderService;
import com.example.reactivecrud.product.dto.ProductResponse;
import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Test
    void createShouldReturn201WithLocation() {
        OrderController controller = new OrderController(orderService);
        OrderRequest request = new OrderRequest(3L, 2);
        when(orderService.create(request)).thenReturn(Mono.just(new OrderResponse(9L, 3L, 2)));

        StepVerifier.create(controller.create(request))
                .assertNext(entity -> {
                    Assertions.assertEquals(HttpStatus.CREATED, entity.getStatusCode());
                    Assertions.assertEquals("/api/orders/9", entity.getHeaders().getLocation().toString());
                    Assertions.assertEquals(9L, entity.getBody().id());
                })
                .verifyComplete();
    }

    @Test
    void findByIdShouldDelegateToService() {
        OrderController controller = new OrderController(orderService);
        when(orderService.findById(9L)).thenReturn(Mono.just(new OrderResponse(9L, 3L, 2)));

        StepVerifier.create(controller.findById(9L))
                .assertNext(response -> Assertions.assertEquals(3L, response.productId()))
                .verifyComplete();

        verify(orderService).findById(9L);
    }

    @Test
    void findDetailsShouldComposeOrderWithProduct() {
        OrderController controller = new OrderController(orderService);
        ProductResponse product = new ProductResponse(3L, "Laptop", "Lightweight", new BigDecimal("999.99"));
        when(orderService.findDetailsById(9L))
                .thenReturn(Mono.just(new OrderDetailsResponse(9L, 2, product, new BigDecimal("1999.98"))));

        StepVerifier.create(controller.findDetailsById(9L))
                .assertNext(details -> {
                    Assertions.assertEquals("Laptop", details.product().name());
                    Assertions.assertEquals(new BigDecimal("1999.98"), details.totalPrice());
                })
                .verifyComplete();

        verify(orderService).findDetailsById(9L);
    }
}
