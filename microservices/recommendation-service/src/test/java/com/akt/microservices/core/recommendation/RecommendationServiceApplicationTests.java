package com.akt.microservices.core.recommendation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class RecommendationServiceApplicationTests {

	@Autowired
	private WebTestClient webTestClient;

	//@Test
	void contextLoads() {
	}

	//@Test
	void getRecommendationsOk(){
		int productId = 1;

		webTestClient.get()
				.uri("/recommendation?productId=" + productId)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody()
					.jsonPath("$.length()").isEqualTo(3)
					.jsonPath("$[0].productId").isEqualTo(productId);
	}

	//@Test
	void getRecommendationsNotFound() {
		int productId = 113;

		webTestClient.get()
				.uri("/recommendation?productId=" + productId)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody()
					.jsonPath("$.length()").isEqualTo(0);
	}

	//@Test
	void getRecommendationsMissingParameter(){
		webTestClient.get()
				.uri("/recommendation")
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(BAD_REQUEST)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody()
					.jsonPath("$.path").isEqualTo("/recommendation")
					.jsonPath("$.message").isEqualTo("Required query parameter 'productId' is not present.");
	}

	//@Test
	void getRecommendationsInvalidParameter(){
		webTestClient.get()
				.uri("/recommendation?productId=no-integer")
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(BAD_REQUEST)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody()
					.jsonPath("$.path").isEqualTo("/recommendation")
					.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	//@Test
	void getRecommendationsInvalidParameterNegativeValue(){
		int productId = -1;

		webTestClient.get()
				.uri("/recommendation?productId=" + productId)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(UNPROCESSABLE_ENTITY)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.path").isEqualTo("/recommendation")
				.jsonPath("$.message").isEqualTo("Invalid productId: "  + productId);
	}
}
