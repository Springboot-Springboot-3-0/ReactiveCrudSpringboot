package com.example.reactivecrud.product.dto;

import com.example.reactivecrud.product.model.Product;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Plain DTO for outgoing product data (response body).
 * Getters are used when Jackson serializes to JSON; the no-arg constructor and
 * setters let it also be deserialized (e.g. by clients reading this response).
 */
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;

    public ProductResponse() {
    }

    public ProductResponse(Long id, String name, String description, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public static ProductResponse from(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getDescription(), product.getPrice());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    // Value equality (like a record) so responses can be compared by their contents.
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProductResponse other)) {
            return false;
        }
        return Objects.equals(id, other.id)
                && Objects.equals(name, other.name)
                && Objects.equals(description, other.description)
                && Objects.equals(price, other.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, price);
    }

    @Override
    public String toString() {
        return "ProductResponse{id=%d, name='%s', description='%s', price=%s}"
                .formatted(id, name, description, price);
    }
}
