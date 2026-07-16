package com.example.reactivecrud.product.streaming.controller;

import com.example.reactivecrud.product.dto.ProductResponse;
import com.example.reactivecrud.product.streaming.service.ProductStreamingService;
import java.time.Duration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/products/stream")
public class ProductStreamingController {

    private final ProductStreamingService productStreamingService;

    public ProductStreamingController(ProductStreamingService productStreamingService) {
        this.productStreamingService = productStreamingService;
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ProductResponse> streamProducts(@RequestParam(defaultValue = "1") long intervalSeconds) {
        long safeInterval = Math.max(1, intervalSeconds);
        return productStreamingService.streamProducts(Duration.ofSeconds(safeInterval));
    }
}
