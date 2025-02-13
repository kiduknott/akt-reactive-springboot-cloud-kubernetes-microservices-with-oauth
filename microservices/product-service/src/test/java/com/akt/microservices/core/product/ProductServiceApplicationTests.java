package com.akt.microservices.core.product;

import com.akt.api.core.product.Product;
import com.akt.microservices.core.product.persistence.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductServiceApplicationTests extends MongoDbTestBase{

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private ProductRepository repository;

	@BeforeEach
	void setupDb() {
		repository.deleteAll();
	}

	@Test
	void contextLoads() {
	}

	@Test
	void getProductByProductId(){
		int productId = 1;

		postAndVerifyProduct(productId, OK);

		assertTrue(repository.findByProductId(productId).isPresent());

		getAndVerifyProduct(productId, OK)
				.jsonPath("$.productId").isEqualTo(productId);
	}

	@Test
	void getProductNotFound() {
		int productId = 123;

		getAndVerifyProduct(productId, NOT_FOUND)
				.jsonPath("$.path").isEqualTo("/product/" + productId)
				.jsonPath("$.message").isEqualTo("No product found for productId: " + productId);
	}

	@Test
	void getProductInvalidParameter(){

		getAndVerifyProduct("/no-integer", BAD_REQUEST)
					.jsonPath("$.path").isEqualTo("/product/no-integer")
					.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	void getProductInvalidParameterNegativeValue(){
		int productId = -1;

		getAndVerifyProduct(productId, UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/product/" + productId)
				.jsonPath("$.message").isEqualTo("Invalid productId: "  + productId);
	}

	@Test
	void duplicateError() {

		int productId = 1;

		postAndVerifyProduct(productId, OK);

		assertTrue(repository.findByProductId(productId).isPresent());

		postAndVerifyProduct(productId, UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/product")
				.jsonPath("$.message").isEqualTo("Duplicate key, Product Id: " + productId);
	}

	@Test
	void deleteProduct() {

		int productId = 1;

		postAndVerifyProduct(productId, OK);
		assertTrue(repository.findByProductId(productId).isPresent());

		deleteAndVerifyProduct(productId, OK);
		assertFalse(repository.findByProductId(productId).isPresent());

		deleteAndVerifyProduct(productId, OK);
	}

	private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus){
		return getAndVerifyProduct("/" + productId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyProduct(String productIdPath, HttpStatus expectedStatus){
		return webTestClient.get()
				.uri("/product" + productIdPath)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody();
	}
	private WebTestClient.BodyContentSpec postAndVerifyProduct(int productId, HttpStatus expectedStatus){
		Product product = new Product(productId,
				"Name - " + productId,
				1,
				"Service Address - " + productId);

		return webTestClient.post()
				.uri("/product")
				.body(just(product), Product.class)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody();
	}

	private WebTestClient.BodyContentSpec deleteAndVerifyProduct(int productId, HttpStatus expectedStatus){
		return webTestClient.delete()
				.uri("/product/" + productId)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectBody();
	}
}
