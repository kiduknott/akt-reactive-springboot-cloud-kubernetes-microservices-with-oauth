package com.akt.microservices.core.product.services;

import com.akt.api.core.product.Product;
import com.akt.api.core.product.ProductService;
import com.akt.api.event.Event;
import com.akt.api.exceptions.EventProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class MessageProcessorConfig {

    private static final Logger logger = LoggerFactory.getLogger(MessageProcessorConfig.class);

    private final ProductService productService;

    @Autowired
    public MessageProcessorConfig(ProductService productService) {
        this.productService = productService;
    }

    @Bean
    public Consumer<Event<Integer, Product>> messageProcessor(){
        return event -> {
            logger.info("Processing the {} message created at {}", event.getEventType(), event.getEventCreatedAt());

            switch (event.getEventType()){
                case CREATE -> {
                    Product product = event.getData();
                    logger.info("Creating product with productId: {}", product.getProductId());
                    productService.createProduct(product).block();
                }

                case DELETE -> {
                    int productId = event.getKey();
                    logger.info("Creating product with productId: {}", productId);
                    productService.deleteProduct(productId).block();
                }

                default -> {
                    String errorMessage = "Incorrect event type: " + event.getEventType() + ". Expected a CREATE or DELETE event";
                    logger.warn(errorMessage);
                    throw new EventProcessingException(errorMessage);
                }
            }

            logger.info("Processing Done - {} message created at {}", event.getEventType(), event.getEventCreatedAt());
        };
    }
}
