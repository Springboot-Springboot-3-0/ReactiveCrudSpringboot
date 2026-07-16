package com.example.reactivecrud.product.repository;

import com.example.reactivecrud.product.model.Product;
import java.math.BigDecimal;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ProductRepository extends ReactiveCrudRepository<Product, Long> {

	@Query("SELECT * FROM products WHERE LOWER(name) LIKE LOWER(CONCAT('%', :name, '%'))")
	Flux<Product> findByNameContainingIgnoreCase(String name);

	@Query("SELECT * FROM products WHERE price BETWEEN :minPrice AND :maxPrice")
	Flux<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
}
