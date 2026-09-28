package com.prguardian.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Permissive CORS for local/demo use: the React dev server and the Docker
 * frontend both run on different origins than the backend, so without this
 * the browser blocks every API call with a CORS error before it even
 * reaches our controllers.
 *
 * DELIBERATELY NOT production-ready as written — allowedOrigins("*") is
 * fine for a local demo but would need to be a specific allowlist of real
 * frontend domains in production, typically enforced at a gateway/reverse
 * proxy layer rather than in application code. Worth stating this
 * limitation unprompted if asked — it's a one-line config change, not a
 * design flaw, but pretending it's already production-hardened would be
 * the wrong answer in an interview.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}