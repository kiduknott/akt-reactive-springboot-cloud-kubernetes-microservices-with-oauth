package com.akt.microservices.composite.product;

import com.akt.api.composite.product.ProductAggregate;
import com.akt.api.composite.product.RecommendationSummary;
import com.akt.api.composite.product.ReviewSummary;
import com.akt.api.core.product.Product;
import com.akt.api.core.recommendation.Recommendation;
import com.akt.api.core.review.Review;
import com.akt.api.event.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.ArrayList;
import java.util.List;

import static com.akt.api.event.Event.Type.CREATE;
import static com.akt.api.event.Event.Type.DELETE;
import static com.akt.microservices.composite.product.IsSameEventMatcher.sameEventExceptCreatedAt;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        properties = {"spring.main.allow-bean-definition-overriding=true"})
@Import({TestChannelBinderConfiguration.class})
class MessagingTests {

    private static final Logger logger = LoggerFactory.getLogger(MessagingTests.class);

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private OutputDestination target;

    @BeforeEach
    void setUp() {
        purgeMessages("products");
        purgeMessages("recommendations");
        purgeMessages("reviews");
    }

    @Test
    void createCompositeProductWithRecommendationsAndReviews() {
        ProductAggregate composite = new ProductAggregate(1, "name", 2,
                singletonList(new RecommendationSummary(1, "a", 3, "c")),
                singletonList(new ReviewSummary(1, "a1", "s1", "c1")), null);

        postAndVerifyProduct(composite, ACCEPTED);

        final List<String> productMessages = getMessages("products");
        final List<String> recommendationMessages = getMessages("recommendations");
        final List<String> reviewMessages = getMessages("reviews");

        // Assertions on product event queue
        assertEquals(1, productMessages.size());
        Event<Integer, Product> expectedProductEvent = new Event<>(CREATE,
                composite.getProductId(), new Product(composite.getProductId(), composite.getName(), composite.getWeight(),null));
        String productMessage = productMessages.get(0);
        assertThat(productMessage, is(sameEventExceptCreatedAt(expectedProductEvent)));

        // Assertions on recommendations event queue
        assertEquals(1, recommendationMessages.size());
        RecommendationSummary recommendationSummary = composite.getRecommendations().get(0);
        Event<Integer, Recommendation> expectedRecommendationEvent = new Event<>(CREATE, composite.getProductId(), new Recommendation(
                composite.getProductId(),
                recommendationSummary.getRecommendationId(),
                recommendationSummary.getAuthor(),
                recommendationSummary.getRating(),
                recommendationSummary.getContent(),
                null));
        String recommendationMessage = recommendationMessages.get(0);
        assertThat(recommendationMessage, is(sameEventExceptCreatedAt(expectedRecommendationEvent)));

        // Assertions on reviews event queue
        assertEquals(1, reviewMessages.size());
        ReviewSummary reviewSummary = composite.getReviews().get(0);
        Event<Integer, Review> expectedReviewEvent = new Event<>(CREATE, composite.getProductId(), new Review(
                composite.getProductId(),
                reviewSummary.getReviewId(),
                reviewSummary.getAuthor(),
                reviewSummary.getSubject(),
                reviewSummary.getContent(),
                null));
        String reviewMessage = reviewMessages.get(0);
        assertThat(reviewMessage, is(sameEventExceptCreatedAt(expectedReviewEvent)));
    }

    @Test
    void createCompositeProductNoRecommendationsOrReviews() {
        ProductAggregate composite = new ProductAggregate(1, "name", 2, null, null, null);

        postAndVerifyProduct(composite, ACCEPTED);

        final List<String> productMessages = getMessages("products");
        final List<String> recommendationMessages = getMessages("recommendations");
        final List<String> reviewMessages = getMessages("reviews");

        // Assertions on product event queue
        assertEquals(1, productMessages.size());
        Event<Integer, Product> expectedProductEvent = new Event<>(CREATE,
                composite.getProductId(), new Product(composite.getProductId(), composite.getName(), composite.getWeight(),null));
        String productMessage = productMessages.get(0);
        assertThat(productMessage, is(sameEventExceptCreatedAt(expectedProductEvent)));

        // Assertions on recommendations event queue
        assertEquals(0, recommendationMessages.size());

        // Assertions on reviews event queue
        assertEquals(0, reviewMessages.size());
    }

    @Test
    void deleteCompositeProduct() {
        deleteAndVerifyProduct(1, ACCEPTED);

        final List<String> productMessages = getMessages("products");
        final List<String> recommendationMessages = getMessages("recommendations");
        final List<String> reviewMessages = getMessages("reviews");

        // Assertions on product event queue
        assertEquals(1, productMessages.size());
        Event<Integer, Product> expectedProductEvent = new Event<>(DELETE, 1, null);
        String productMessage = productMessages.get(0);
        assertThat(productMessage, is(sameEventExceptCreatedAt(expectedProductEvent)));

        // Assertions on recommendations event queue
        Event<Integer, Recommendation> expectedRecommendationEvent = new Event<>(DELETE, 1, null);
        String recommendationMessage = recommendationMessages.get(0);
        assertThat(recommendationMessage, is(sameEventExceptCreatedAt(expectedRecommendationEvent)));

        // Assertions on reviews event queue
        assertEquals(1, reviewMessages.size());
        Event<Integer, Review> expectedReviewEvent = new Event<>(DELETE, 1, null);
        String reviewMessage = reviewMessages.get(0);
        assertThat(reviewMessage, is(sameEventExceptCreatedAt(expectedReviewEvent)));
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

    private List<String> getMessages(String bindingName) {
        List<String> messages = new ArrayList<>();
        boolean anyMoreMessages = true;

        while(anyMoreMessages) {
            Message<byte[]> message = getMessage(bindingName);

            if(message == null){
                anyMoreMessages = false;
            } else {
                messages.add(new String(message.getPayload()));
            }
        }

        return messages;
    }

    private Message<byte[]> getMessage(String bindingName) {
        try{
            return target.receive(0, bindingName);
        } catch (NullPointerException exception){
            logger.error("getMessage(): null pointer exception thrown for binding: {}", bindingName);
            return null;
        }
    }

    private void purgeMessages(String bindingName) {
        getMessages(bindingName);
    }
}
