package com.example.reactivecrud.order.service;

import com.example.reactivecrud.order.dto.OrderDetailsResponse;
import com.example.reactivecrud.order.dto.OrderRequest;
import com.example.reactivecrud.order.dto.OrderResponse;
import com.example.reactivecrud.order.exception.OrderNotFoundException;
import com.example.reactivecrud.order.model.Order;
import com.example.reactivecrud.order.repository.OrderRepository;
import com.example.reactivecrud.product.dto.ProductResponse;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient webClient;

    public OrderService(
            OrderRepository orderRepository,
            @Value("${app.self-base-url:http://localhost:8080}") String selfBaseUrl
    ) {
        this.orderRepository = orderRepository;
        this.webClient = WebClient.builder().baseUrl(selfBaseUrl).build();
    }

    public Flux<OrderResponse> findAll() {
        return orderRepository.findAll().map(OrderResponse::from);
    }

    public Mono<OrderResponse> create(OrderRequest request) {
        Order order = new Order(null, request.productId(), request.quantity());
        return orderRepository.save(order).map(OrderResponse::from);
    }

    public Mono<OrderResponse> findById(Long id) {
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(new OrderNotFoundException(id)))
                .map(OrderResponse::from);
    }

    public Mono<OrderDetailsResponse> findDetailsById(Long id) {
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(new OrderNotFoundException(id)))
                .flatMap(order -> fetchProduct(order.getProductId())
                        .map(product -> new OrderDetailsResponse(
                                order.getId(),
                                order.getQuantity(),
                                product,
                                product.price().multiply(BigDecimal.valueOf(order.getQuantity()))
                        )));
    }

    private Mono<ProductResponse> fetchProduct(Long productId) {
        return webClient.get()
                .uri("/api/products/{id}", productId)
                .retrieve()
                .bodyToMono(ProductResponse.class);
    }
}
