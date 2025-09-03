package com.redligot.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

@Configuration
@ConfigurationProperties(prefix = "app")
public class JwtConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtConfig.class);
    
    private String jwtSecret;
    private int jwtExpirationInMs;
    
    public String getJwtSecret() {
        return jwtSecret;
    }
    
    public void setJwtSecret(String jwtSecret) {
        logger.info("JwtConfig: Setting jwtSecret to: {}...", jwtSecret != null ? jwtSecret.substring(0, Math.min(10, jwtSecret.length())) : "null");
        this.jwtSecret = jwtSecret;
    }
    
    public int getJwtExpirationInMs() {
        return jwtExpirationInMs;
    }
    
    public void setJwtExpirationInMs(int jwtExpirationInMs) {
        logger.info("JwtConfig: Setting jwtExpirationInMs to: {}", jwtExpirationInMs);
        this.jwtExpirationInMs = jwtExpirationInMs;
    }
    
    @PostConstruct
    public void logConfiguration() {
        logger.info("JwtConfig: Final configuration - jwtSecret: {}..., jwtExpirationInMs: {}", 
            jwtSecret != null ? jwtSecret.substring(0, Math.min(10, jwtSecret.length())) : "null", 
            jwtExpirationInMs);
    }
}
