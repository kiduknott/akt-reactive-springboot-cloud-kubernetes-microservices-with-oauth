package com.akt.microservices.core.review;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.akt")
public class ReviewServiceApplication {

	private static final Logger logger = LoggerFactory.getLogger(ReviewServiceApplication.class);

	public static void main(String[] args) {
		ConfigurableApplicationContext applicationContext = SpringApplication.run(ReviewServiceApplication.class, args);

		String mysqlUri = applicationContext.getEnvironment().getProperty("spring.datasource.url");
		logger.info("Connected to MySQL: " + mysqlUri);
	}

}
