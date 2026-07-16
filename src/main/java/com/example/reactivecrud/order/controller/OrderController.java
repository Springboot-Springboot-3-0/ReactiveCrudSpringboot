package com.example.reactivecrud.order.controller;

import com.example.reactivecrud.order.dto.OrderDetailsResponse;
import com.example.reactivecrud.order.dto.OrderRequest;
import com.example.reactivecrud.order.dto.OrderResponse;
import com.example.reactivecrud.order.service.OrderService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public Flux<OrderResponse> findAll() {
        return orderService.findAll();
    }

    @PostMapping
    public Mono<ResponseEntity<OrderResponse>> create(@Valid @RequestBody OrderRequest request) {
        return orderService.create(request)
                .map(response -> ResponseEntity.created(URI.create("/api/orders/" + response.id())).body(response));
    }

    @GetMapping("/{id}")
    public Mono<OrderResponse> findById(@PathVariable Long id) {
        return orderService.findById(id);
    }

    @GetMapping("/{id}/details")
    public Mono<OrderDetailsResponse> findDetailsById(@PathVariable Long id) {
        return orderService.findDetailsById(id);
    }
}
