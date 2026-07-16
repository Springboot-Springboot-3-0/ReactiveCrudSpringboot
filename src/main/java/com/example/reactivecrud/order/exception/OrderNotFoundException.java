package com.example.reactivecrud.order.exception;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long id) {
        super("Order %d was not found".formatted(id));
    }
}
