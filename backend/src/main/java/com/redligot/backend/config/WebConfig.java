package com.redligot.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

/**
 * Web configuration for Spring Data pagination support.
 * This enables proper pagination serialization and suppresses warnings.
 */
@Configuration
@EnableSpringDataWebSupport
public class WebConfig {
}
