package com.redligot.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Spring Boot entry point for the Photo Upload CRUD backend.
 *
 * <p>This application exposes REST endpoints under <code>/api/photos</code>
 * to create, read, update, delete, and download photo content. Images are
 * stored directly in an in-memory H2 database for simplicity.</p>
 */
@SpringBootApplication
@EnableConfigurationProperties
public class BackendApplication {

	/**
	 * Bootstraps the Spring application.
	 *
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
