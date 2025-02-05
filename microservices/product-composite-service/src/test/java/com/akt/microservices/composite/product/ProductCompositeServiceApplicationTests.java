package com.akt.microservices.composite.product;

import com.akt.api.core.product.Product;
import com.akt.api.core.recommendation.Recommendation;
import com.akt.api.core.review.Review;
import com.akt.api.exceptions.InvalidInputException;
import com.akt.api.exceptions.NotFoundException;
import com.akt.microservices.composite.product.services.ProductCompositeIntegration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductCompositeServiceApplicationTests {

	private static final int PRODUCT_ID_OK = 1;
	private static final int PRODUCT_ID_NOT_FOUND = 2;
	private static final int PRODUCT_ID_INVALID_INPUT = 3;

	@Autowired
	private WebTestClient webTestClient;

	@MockitoBean
	private ProductCompositeIntegration productCompositeIntegration;

	@BeforeEach
	void setUp(){
		when(productCompositeIntegration.getProduct(PRODUCT_ID_OK))
				.thenReturn(new Product(PRODUCT_ID_OK, "name", 1, "mock-address"));
		when(productCompositeIntegration.getRecommendations(PRODUCT_ID_OK))
				.thenReturn(singletonList(new Recommendation(PRODUCT_ID_OK, 1, "author", 1, "content", "mock-address")));
		when(productCompositeIntegration.getReviews(PRODUCT_ID_OK))
				.thenReturn(singletonList(new Review(PRODUCT_ID_OK, 1, "author", "subject", "content", "mock-address")));

		when(productCompositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND))
				.thenThrow(new NotFoundException("NOT FOUND: " + PRODUCT_ID_NOT_FOUND));

		when(productCompositeIntegration.getProduct(PRODUCT_ID_INVALID_INPUT))
				.thenThrow(new InvalidInputException("INVALID: " + PRODUCT_ID_INVALID_INPUT));
	}

	@Test
	void contextLoads() {
	}

	@Test
	void getProductOk(){
		webTestClient.get()
				.uri("/product-composite/" + PRODUCT_ID_OK)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody()
					.jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
					.jsonPath("$.recommendations.length()").isEqualTo(1)
					.jsonPath("$.reviews.length()").isEqualTo(1);

	}

	@Test
	void getProductNotFound(){
		webTestClient.get()
				.uri("/product-composite/" + PRODUCT_ID_NOT_FOUND)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isNotFound()
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_NOT_FOUND)
				.jsonPath("$.message").isEqualTo("NOT FOUND: " + PRODUCT_ID_NOT_FOUND);

	}

	@Test
	void getProductInvalidInput(){
		webTestClient.get()
				.uri("/product-composite/" + PRODUCT_ID_INVALID_INPUT)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(UNPROCESSABLE_ENTITY)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_INVALID_INPUT)
				.jsonPath("$.message").isEqualTo("INVALID: " + PRODUCT_ID_INVALID_INPUT);

	}

}
