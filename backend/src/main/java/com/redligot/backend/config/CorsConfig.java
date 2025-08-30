package com.redligot.backend.config;

import org.springframework.context.annotation.Configuration;

/**
 * CORS configuration is now handled entirely by SecurityConfig.
 * This class is kept for potential future WebMvc-specific configurations.
 */
@Configuration
public class CorsConfig {
    // CORS configuration is now handled by SecurityConfig
    // WebMvc CORS has been removed to avoid conflicts with Spring Security CORS
}
