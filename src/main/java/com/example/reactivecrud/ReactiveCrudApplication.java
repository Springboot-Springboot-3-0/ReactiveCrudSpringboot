package com.example.reactivecrud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Topic: End-to-End Reactive Microservices
 * This branch shows how reactive services can talk to each other in a non-blocking way.
 * It helps build responsive systems that handle many requests efficiently.
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
