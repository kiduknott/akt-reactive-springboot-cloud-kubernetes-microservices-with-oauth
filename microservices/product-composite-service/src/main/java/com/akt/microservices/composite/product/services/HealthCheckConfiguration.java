package com.akt.microservices.composite.product.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class HealthCheckConfiguration {

    @Autowired
    ProductCompositeIntegration productCompositeIntegration;

    @Bean
    ReactiveHealthContributor coreServices(){
        final Map<String, ReactiveHealthIndicator> registry = new LinkedHashMap<>();

        registry.put("product", () -> productCompositeIntegration.getProductHealth());
        registry.put("recommendation", () -> productCompositeIntegration.getRecommendationHealth());
        registry.put("review", () -> productCompositeIntegration.getReviewHealth());

        return CompositeReactiveHealthContributor.fromMap(registry);
    }
}
