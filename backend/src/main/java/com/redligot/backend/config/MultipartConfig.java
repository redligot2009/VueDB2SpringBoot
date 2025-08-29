package com.redligot.backend.config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import jakarta.servlet.MultipartConfigElement;

/**
 * Configuration for multipart file upload handling.
 * Ensures proper handling of multipart/form-data requests.
 */
@Configuration
public class MultipartConfig {

    /**
     * Configure multipart resolver for file uploads.
     * Uses StandardServletMultipartResolver which is the recommended approach for Spring Boot 3.x.
     * 
     * @return configured multipart resolver
     */
    @Bean
    public MultipartResolver multipartResolver() {
        StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
        System.out.println("Using StandardServletMultipartResolver for multipart handling");
        return resolver;
    }

    /**
     * Configure multipart configuration for servlet.
     * This is the key configuration that sets up the multipart handling.
     * 
     * @return multipart configuration element
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        
        // Set maximum file size (10MB)
        factory.setMaxFileSize(DataSize.ofMegabytes(10));
        
        // Set maximum request size (20MB for bulk uploads)
        factory.setMaxRequestSize(DataSize.ofMegabytes(20));
        
        // Set file size threshold (2KB - files smaller than this will be stored in memory)
        factory.setFileSizeThreshold(DataSize.ofKilobytes(2));
        
        System.out.println("MultipartConfig: Configured with maxFileSize=10MB, maxRequestSize=20MB");
        
        return factory.createMultipartConfig();
    }
}
