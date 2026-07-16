package com.example.reactivecrud.order.repository;

import com.example.reactivecrud.order.model.Order;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderRepository {

    Flux<Order> findAll();

    Mono<Order> findById(Long id);

    Mono<Order> save(Order order);
}
