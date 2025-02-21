package com.akt.microservices.composite.product.services;

import com.akt.api.core.product.Product;
import com.akt.api.core.product.ProductService;
import com.akt.api.core.recommendation.Recommendation;
import com.akt.api.core.recommendation.RecommendationService;
import com.akt.api.core.review.Review;
import com.akt.api.core.review.ReviewService;
import com.akt.api.event.Event;
import com.akt.api.exceptions.InvalidInputException;
import com.akt.api.exceptions.NotFoundException;
import com.akt.util.http.HttpErrorInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.io.IOException;
import java.util.Objects;

import static com.akt.api.event.Event.Type.CREATE;
import static com.akt.api.event.Event.Type.DELETE;
import static java.util.logging.Level.FINE;
import static reactor.core.publisher.Flux.empty;

@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ProductCompositeIntegration.class);

    private final WebClient webClient;
    private final ObjectMapper mapper;

    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;

    private final StreamBridge streamBridge;
    private final Scheduler publishEventScheduler;

    @Autowired
    public ProductCompositeIntegration (
            @Qualifier("publishEventScheduler") Scheduler publishEventScheduler,
            WebClient.Builder webClientBuilder,
            ObjectMapper mapper,
            StreamBridge streamBridge,
            @Value("${app.product-service.host}") String productServiceHost,
            @Value("${app.product-service.port}") String productServicePort,
            @Value("${app.recommendation-service.host}") String recommendationServiceHost,
            @Value("${app.recommendation-service.port}") String recommendationServicePort,
            @Value("${app.review-service.host}") String reviewServiceHost,
            @Value("${app.review-service.port}") String reviewServicePort) {

        this.publishEventScheduler = publishEventScheduler;
        this.webClient = webClientBuilder.build();
        this.mapper = mapper;
        this.streamBridge = streamBridge;

        productServiceUrl = "http://" + productServiceHost + ":" + productServicePort;
        recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort;
        reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort;
    }

    public Mono<Product> getProduct(int productId){
        String url = productServiceUrl +  "/product/" + productId;
        logger.debug("Calling getProduct API on URL: {}", url);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(Product.class)
                .log(logger.getName(), FINE)
                .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
    }

    public Flux<Recommendation> getRecommendations(int productId){
        String url = recommendationServiceUrl + "/recommendation?productId=" + productId;
        logger.debug("Calling getRecommendations API on URL: {}", url);

        // Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
        Flux<Recommendation> recommendationFlux = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(Recommendation.class)
                .log(logger.getName(), FINE)
                .onErrorResume(error -> empty());

        logger.debug("getRecommendations - returned recommendations for URL: {}", url);
        return  recommendationFlux;
    }

    public Flux<Review> getReviews(int productId) {
        String url = reviewServiceUrl + "/review?productId=" + productId;
        logger.debug("Calling getReviews API on URL: {}", url);

        // Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
        Flux<Review> reviewsFlux = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(Review.class)
                .log(logger.getName(), FINE)
                .onErrorResume(error -> empty());

        logger.debug("getReviews - returned reviews for URL: {}", url);
        return  reviewsFlux;
    }

    public Mono<Product> createProduct(Product body) {
        int productId = body.getProductId();
        logger.debug("Posting a new product with productId: {}", productId);

        Mono<Product> productMono = Mono.fromCallable(
                () -> {
                    sendMessage("products-out-0", new Event<>(CREATE, productId, body));
                    return body;
                }).subscribeOn(publishEventScheduler);

        logger.debug("Created a product with productId: {}", productId);
        return productMono;
    }

    public Mono<Recommendation> createRecommendation(Recommendation body) {
        int productId = body.getProductId();
        int recommendationId = body.getRecommendationId();
        logger.debug("Posting a new recommendation for productId: {} recommendationId: {}", productId, recommendationId);

        Mono<Recommendation> recommendationMono =  Mono.fromCallable(
                () -> {
                    sendMessage("recommendations-out-0", new Event<>(CREATE, productId, body));
                    return body;
                }).subscribeOn(publishEventScheduler);

        logger.debug("Created a recommendation for productId: {} with recommendationId: {}", productId, recommendationId);
        return recommendationMono;
    }

    public Mono<Review> createReview(Review body) {
        int productId = body.getProductId();
        int reviewId = body.getReviewId();
        logger.debug("Posting a new review for productId: {} reviewId: {}", productId, reviewId);

        Mono<Review> reviewMono =  Mono.fromCallable(
                () -> {
                    sendMessage("reviews-out-0", new Event<>(CREATE, productId, body));
                    return body;
                }).subscribeOn(publishEventScheduler);

        logger.debug("Created a review for productId: {}", productId);
        return reviewMono;
    }

    public Mono<Void> deleteProduct(int productId) {
        logger.debug("Posting a delete product for productId: {}", productId);

        Mono<Void> productMono = Mono.fromRunnable(
                () -> sendMessage("products-out-0", new Event<>(DELETE, productId, null)))
                .subscribeOn(publishEventScheduler).then();

        logger.debug("Delete attempt for product with productId: {}", productId);
        return productMono;
    }

    public Mono<Void> deleteRecommendations(int productId) {
        logger.debug("Posting delete recommendations for productId: {}", productId);

        Mono<Void> recommendationMono = Mono.fromRunnable(
                        () -> sendMessage("recommendations-out-0", new Event<>(DELETE, productId, null)))
                .subscribeOn(publishEventScheduler).then();

        logger.debug("Delete attempt of recommendations of productId: {}", productId);
        return recommendationMono;
    }

    public Mono<Void> deleteReviews(int productId) {
        logger.debug("Posting delete reviews for productId: {}", productId);

        Mono<Void> reviewMono = Mono.fromRunnable(
                        () -> sendMessage("reviews-out-0", new Event<>(DELETE, productId, null)))
                .subscribeOn(publishEventScheduler).then();

        logger.debug("Delete attempt of reviews of productId: {}", productId);
        return reviewMono;
    }

    private String getErrorMessage(WebClientResponseException exception) {
        try {
            return mapper.readValue(exception.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioException) {
            return exception.getMessage();
        }
    }

    private Throwable handleException(Throwable exception) {

        if (!(exception instanceof WebClientResponseException)) {
            logger.warn("Got a unexpected error: {}, will rethrow it", exception.toString());
            return exception;
        }

        WebClientResponseException webClientResponseException = (WebClientResponseException)exception;

        switch (Objects.requireNonNull(HttpStatus.resolve(webClientResponseException.getStatusCode().value()))) {

            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(webClientResponseException));

            case UNPROCESSABLE_ENTITY:
                return new InvalidInputException(getErrorMessage(webClientResponseException));

            default:
                logger.warn("Got an unexpected HTTP error: {}, will rethrow it", webClientResponseException.getStatusCode());
                logger.warn("Error body: {}", webClientResponseException.getResponseBodyAsString());
                return exception;
        }
    }

    private void sendMessage(String bindingName, Event event) {
        logger.debug("Sending a {} message to {}", event.getEventType(), bindingName);
        Message<Event> message = MessageBuilder.withPayload(event)
                .setHeader("partitionKey", event.getKey())
                .build();
        streamBridge.send(bindingName, message);
    }
}
