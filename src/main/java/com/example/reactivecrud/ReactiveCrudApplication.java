package com.example.reactivecrud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Topic: Input Validation and Reactive Error Handling
 * This branch explains how to validate incoming data and return clear error responses.
 * It keeps reactive flows safe while handling invalid input gracefully.
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
