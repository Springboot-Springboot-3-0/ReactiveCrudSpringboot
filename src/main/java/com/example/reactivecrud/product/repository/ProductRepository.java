package com.example.reactivecrud.product.repository;

import com.example.reactivecrud.product.model.Product;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ProductRepository extends ReactiveCrudRepository<Product, Long> {
}
