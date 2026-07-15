package com.example.reactivecrud.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "name is required")
        @Size(max = 100, message = "name must be at most 100 characters")
        String name,
        @Size(max = 255, message = "description must be at most 255 characters")
        String description,
        @DecimalMin(value = "0.0", inclusive = false, message = "price must be greater than zero")
        BigDecimal price
) {
}
