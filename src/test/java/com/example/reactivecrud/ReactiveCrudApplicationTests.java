package com.example.reactivecrud;

import com.example.reactivecrud.product.model.Product;
import com.example.reactivecrud.product.repository.ProductRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

@SpringBootTest
class ReactiveCrudApplicationTests {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void contextLoads() {
        StepVerifier.create(productRepository.save(new Product(null, "Test", "App context", new BigDecimal("10.00"))))
                .assertNext(saved -> {
                    Assertions.assertNotNull(saved.getId());
                    Assertions.assertEquals("Test", saved.getName());
                })
                .verifyComplete();
    }

}
