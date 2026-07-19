package com.example.reactivecrud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Topic: Reactive Data Streaming
 * This branch shows how to stream data continuously to clients.
 * It is useful for real-time updates and event-style responses.
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
