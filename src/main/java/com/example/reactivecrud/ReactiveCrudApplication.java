package com.example.reactivecrud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Topic: Reactive Data Access with Spring Data R2DBC
 * This branch explains non-blocking database access using Spring Data R2DBC.
 * It avoids blocking threads while reading and writing data.
 */
@SpringBootApplication
public class ReactiveCrudApplication {

	/**
	 * Starts the Spring Boot application.
	 */
	public static void main(String[] args) {
		SpringApplication.run(ReactiveCrudApplication.class, args);
	}

}
