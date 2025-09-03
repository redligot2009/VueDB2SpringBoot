package com.redligot.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class JwtConfig {
    
    private String jwtSecret;
    private int jwtExpirationInMs;
    
    public String getJwtSecret() {
        return jwtSecret;
    }
    
    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }
    
    public int getJwtExpirationInMs() {
        return jwtExpirationInMs;
    }
    
    public void setJwtExpirationInMs(int jwtExpirationInMs) {
        this.jwtExpirationInMs = jwtExpirationInMs;
    }
}
