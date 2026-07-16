package com.example.reactivecrud.order.dto;

import com.example.reactivecrud.order.model.Order;

public record OrderResponse(
        Long id,
        Long productId,
        Integer quantity
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(order.getId(), order.getProductId(), order.getQuantity());
    }
}
