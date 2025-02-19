package com.akt.microservices.core.product;

import com.akt.microservices.core.product.persistence.ProductEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;

@SpringBootApplication
@ComponentScan("com.akt")
public class ProductServiceApplication {

	private static final Logger logger = LoggerFactory.getLogger(ProductServiceApplication.class);

	@Autowired
	ReactiveMongoOperations mongoTemplate;

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(ProductServiceApplication.class, args);

		String mongoDbHost = context.getEnvironment().getProperty("spring.data.mongodb.host");
		String mongoDbPort = context.getEnvironment().getProperty("spring.data.mongodb.port");
		logger.info("Connected to MongoDb: " + mongoDbHost + ":" + mongoDbPort);
	}

	@EventListener(ContextRefreshedEvent.class)
	public void initIndicesAfterStartup(){
		MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext = mongoTemplate.getConverter().getMappingContext();
		IndexResolver resolver = new MongoPersistentEntityIndexResolver(mappingContext);

		ReactiveIndexOperations indexOperations = mongoTemplate.indexOps(ProductEntity.class);
		resolver.resolveIndexFor(ProductEntity.class).forEach(e -> indexOperations.ensureIndex(e).block());
	}
}
