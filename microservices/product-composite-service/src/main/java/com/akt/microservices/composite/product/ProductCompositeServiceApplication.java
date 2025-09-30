package com.akt.microservices.composite.product;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@SpringBootApplication
@ComponentScan("com.akt")
public class ProductCompositeServiceApplication {

	private static final Logger logger = LoggerFactory.getLogger(ProductCompositeServiceApplication.class);

	@Value("api.common.version") String apiVersion;
	@Value("api.common.title") String apiTitle;
	@Value("api.common.description") String apiDescription;
	@Value("api.common.termsOfService") String apiTermsOfService;
	@Value("${api.common.license}") String apiLicense;
	@Value("${api.common.licenseUrl}") String apiLicenseUrl;
	@Value("${api.common.externalDocDescription}") String apiExternalDocDescription;
	@Value("api.common.externalDocUrl") String apiExternalDocUrl;
	@Value("${api.common.contact.name}") String apiContactName;
	@Value("${api.common.contact.url}") String apiContactUrl;
	@Value("${api.common.contact.email}") String apiContactEmail;

	private final Integer threadPoolSize;
	private final Integer taskQueueSize;

	@Autowired
	public ProductCompositeServiceApplication(
			@Value("${app.threadPoolSize:10}") Integer threadPoolSize,
			@Value("${app.taskQueueSize:100}") Integer taskQueueSize) {
		this.threadPoolSize = threadPoolSize;
		this.taskQueueSize = taskQueueSize;
	}

	@Bean
	public OpenAPI getOpenApiDocumentation(){
		return new OpenAPI()
				.info(new Info().title(apiTitle)
						.description(apiDescription)
						.version(apiVersion)
						.contact(new Contact()
								.name(apiContactName)
								.url(apiContactUrl)
								.email(apiContactEmail))
						.termsOfService(apiTermsOfService)
						.license(new License()
								.name(apiLicense)
								.url(apiLicenseUrl)))
				.externalDocs(new ExternalDocumentation()
						.description(apiExternalDocDescription)
						.url(apiExternalDocUrl));
	}

	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	//TODO: Commment out this @Bean and see what breaks
	@Bean
	public Scheduler publishEventScheduler(){
		logger.info("Creating a messagingScheduler with threadPoolSize = {}", threadPoolSize);
		return Schedulers.newBoundedElastic(threadPoolSize, taskQueueSize, "publish-pool");
	}

	@Bean
	@LoadBalanced
	public WebClient.Builder loadBalancedWebClientBuilder(){
		return WebClient.builder();
	}

	public static void main(String[] args) {
		SpringApplication.run(ProductCompositeServiceApplication.class, args);
	}
}
