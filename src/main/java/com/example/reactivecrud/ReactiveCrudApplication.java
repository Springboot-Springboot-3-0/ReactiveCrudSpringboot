package com.example.reactivecrud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Topic: Reactive CRUD Basics
 * This branch contains the core reactive CRUD foundation of the application.
 * It demonstrates non-blocking create, read, update, and delete operations.
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
