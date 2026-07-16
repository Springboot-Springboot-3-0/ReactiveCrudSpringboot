package com.example.reactivecrud.order.repository;

import com.example.reactivecrud.order.model.Order;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class InMemoryOrderRepository implements OrderRepository {

    private final Map<Long, Order> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public Flux<Order> findAll() {
        return Flux.fromIterable(store.values());
    }

    @Override
    public Mono<Order> findById(Long id) {
        return Mono.justOrEmpty(store.get(id));
    }

    @Override
    public Mono<Order> save(Order order) {
        if (order.getId() == null) {
            order.setId(sequence.getAndIncrement());
        }
        store.put(order.getId(), order);
        return Mono.just(order);
    }
}
