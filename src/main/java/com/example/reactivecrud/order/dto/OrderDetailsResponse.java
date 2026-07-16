package com.example.reactivecrud.order.dto;

import com.example.reactivecrud.product.dto.ProductResponse;
import java.math.BigDecimal;

public record OrderDetailsResponse(
        Long orderId,
        Integer quantity,
        ProductResponse product,
        BigDecimal totalPrice
) {
}
