package com.zfy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.Arrays;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final String[] origins;
    public WebConfig(@Value("${app.cors-origins:http://localhost:5173,http://127.0.0.1:5173,http://localhost:4173,http://127.0.0.1:4173,http://localhost:4174,http://127.0.0.1:4174,http://localhost:4175,http://127.0.0.1:4175}") String origins) {
        this.origins=Arrays.stream(origins.split(",")).map(String::trim).filter(s->!s.isEmpty()).toArray(String[]::new);
    }
    @Override public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOrigins(origins).allowedMethods("GET","POST","OPTIONS")
                .allowedHeaders("Content-Type","Authorization").maxAge(3600);
    }
}
