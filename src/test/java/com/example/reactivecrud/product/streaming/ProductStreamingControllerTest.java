package com.example.reactivecrud.product.streaming;

import com.example.reactivecrud.product.dto.ProductResponse;
import java.math.BigDecimal;
import java.time.Duration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductStreamingControllerTest {

    @Mock
    private ProductStreamingService productStreamingService;

    @Test
    void streamProductsShouldDelegateToService() {
        ProductStreamingController controller = new ProductStreamingController(productStreamingService);
        when(productStreamingService.streamProducts(Duration.ofSeconds(2)))
                .thenReturn(Flux.just(new ProductResponse(1L, "Laptop", "Lightweight", new BigDecimal("999.99"))));

        StepVerifier.create(controller.streamProducts(2))
                .assertNext(response -> Assertions.assertEquals("Laptop", response.name()))
                .verifyComplete();

        verify(productStreamingService).streamProducts(Duration.ofSeconds(2));
    }
}
