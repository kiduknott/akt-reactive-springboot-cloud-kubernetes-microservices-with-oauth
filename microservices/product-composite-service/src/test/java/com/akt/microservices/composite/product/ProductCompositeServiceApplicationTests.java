package com.akt.microservices.composite.product;

import com.akt.api.composite.product.ProductAggregate;
import com.akt.api.composite.product.RecommendationSummary;
import com.akt.api.composite.product.ReviewSummary;
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
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

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
				.thenReturn(Mono.just(new Product(PRODUCT_ID_OK, "name", 1, "mock-address")));
		when(productCompositeIntegration.getRecommendations(PRODUCT_ID_OK))
				.thenReturn(Flux.fromIterable(singletonList(new Recommendation(PRODUCT_ID_OK, 1, "author", 1, "content", "mock-address"))));
		when(productCompositeIntegration.getReviews(PRODUCT_ID_OK))
				.thenReturn(Flux.fromIterable(singletonList(new Review(PRODUCT_ID_OK, 1, "author", "subject", "content", "mock-address"))));

		when(productCompositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND))
				.thenThrow(new NotFoundException("NOT FOUND: " + PRODUCT_ID_NOT_FOUND));

		when(productCompositeIntegration.getProduct(PRODUCT_ID_INVALID_INPUT))
				.thenThrow(new InvalidInputException("INVALID: " + PRODUCT_ID_INVALID_INPUT));
	}

	@Test
	void contextLoads() {
	}

	@Test
	void getProductByProductId(){
		getAndVerifyProduct(PRODUCT_ID_OK, OK)
					.jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
					.jsonPath("$.recommendations.length()").isEqualTo(1)
					.jsonPath("$.reviews.length()").isEqualTo(1);
	}

	@Test
	void getProductNotFound(){
		getAndVerifyProduct(PRODUCT_ID_NOT_FOUND, NOT_FOUND)
				.jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_NOT_FOUND)
				.jsonPath("$.message").isEqualTo("NOT FOUND: " + PRODUCT_ID_NOT_FOUND);
	}

	@Test
	void getProductInvalidInput(){
		getAndVerifyProduct(PRODUCT_ID_INVALID_INPUT, UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_INVALID_INPUT)
				.jsonPath("$.message").isEqualTo("INVALID: " + PRODUCT_ID_INVALID_INPUT);
	}

	@Test
	void createCompositeProductNoRecommendationsOrReviews() {
		ProductAggregate compositeProduct = new ProductAggregate(1, "name", 1, null, null, null);

		postAndVerifyProduct(compositeProduct, OK);
	}

	@Test
	void createCompositeProductWithRecommendationsOrReviews() {
		ProductAggregate compositeProduct = new ProductAggregate(1, "name", 1,
				singletonList(new RecommendationSummary(1, "a", 1, "c")),
				singletonList(new ReviewSummary(1, "a", "s", "c")), null);

		postAndVerifyProduct(compositeProduct, OK);
	}

	@Test
	void deleteCompositeProduct() {
		ProductAggregate compositeProduct = new ProductAggregate(1, "name", 1,
				singletonList(new RecommendationSummary(1, "a", 1, "c")),
				singletonList(new ReviewSummary(1, "a", "s", "c")), null);

		postAndVerifyProduct(compositeProduct, OK);

		deleteAndVerifyProduct(compositeProduct.getProductId(), OK);
		deleteAndVerifyProduct(compositeProduct.getProductId(), OK);
	}

	private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus) {
		return webTestClient.get()
				.uri("/product-composite/" + productId)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody();
	}

	private void postAndVerifyProduct(ProductAggregate compositeProduct, HttpStatus expectedStatus) {
		webTestClient.post()
				.uri("/product-composite")
				.body(just(compositeProduct), ProductAggregate.class)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus);
	}

	private void deleteAndVerifyProduct(int productId, HttpStatus expectedStatus) {
		webTestClient.delete()
				.uri("/product-composite/" + productId)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus);
	}
}
