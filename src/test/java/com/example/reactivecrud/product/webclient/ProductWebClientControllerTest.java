package com.example.reactivecrud.product.webclient;

import com.example.reactivecrud.product.dto.ProductRequest;
import com.example.reactivecrud.product.dto.ProductResponse;
import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductWebClientControllerTest {

    @Mock
    private ProductWebClientService productWebClientService;

    @Test
    void findAllViaWebClientShouldDelegateToService() {
        ProductWebClientController controller = new ProductWebClientController(productWebClientService);
        when(productWebClientService.findAll())
                .thenReturn(Flux.just(new ProductResponse(1L, "Laptop", "Lightweight", new BigDecimal("999.99"))));

        StepVerifier.create(controller.findAllViaWebClient())
                .assertNext(response -> Assertions.assertEquals("Laptop", response.name()))
                .verifyComplete();

        verify(productWebClientService).findAll();
    }

    @Test
    void findByIdViaWebClientShouldDelegateToService() {
        ProductWebClientController controller = new ProductWebClientController(productWebClientService);
        when(productWebClientService.findById(4L))
                .thenReturn(Mono.just(new ProductResponse(4L, "Phone", "Updated", new BigDecimal("599.99"))));

        StepVerifier.create(controller.findByIdViaWebClient(4L))
                .assertNext(response -> Assertions.assertEquals(4L, response.id()))
                .verifyComplete();

        verify(productWebClientService).findById(4L);
    }

    @Test
    void createViaWebClientShouldDelegateToService() {
        ProductWebClientController controller = new ProductWebClientController(productWebClientService);
        ProductRequest request = new ProductRequest("Keyboard", "Mechanical", new BigDecimal("79.99"));
        when(productWebClientService.create(request))
                .thenReturn(Mono.just(new ProductResponse(8L, "Keyboard", "Mechanical", new BigDecimal("79.99"))));

        StepVerifier.create(controller.createViaWebClient(request))
                .assertNext(response -> Assertions.assertEquals(8L, response.id()))
                .verifyComplete();

        verify(productWebClientService).create(request);
    }
}
