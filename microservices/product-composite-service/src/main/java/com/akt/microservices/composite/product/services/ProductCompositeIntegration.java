package com.akt.microservices.composite.product.services;

import com.akt.api.core.product.Product;
import com.akt.api.core.recommendation.Recommendation;
import com.akt.api.core.review.Review;
import com.akt.api.exceptions.InvalidInputException;
import com.akt.api.exceptions.NotFoundException;
import com.akt.util.http.HttpErrorInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Objects;

import static java.util.logging.Level.FINE;
import static reactor.core.publisher.Flux.empty;

@Component
public class ProductCompositeIntegration {

    private static final Logger logger = LoggerFactory.getLogger(ProductCompositeIntegration.class);

    private final WebClient webClient;
    private final ObjectMapper mapper;

    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;

    @Autowired
    public ProductCompositeIntegration(WebClient.Builder webClientBuilder,
       ObjectMapper mapper,
       @Value("${app.product-service.host}") String productServiceHost,
       @Value("${app.product-service.port}") String productServicePort,
       @Value("${app.recommendation-service.host}") String recommendationServiceHost,
       @Value("${app.recommendation-service.port}") String recommendationServicePort,
       @Value("${app.review-service.host}") String reviewServiceHost,
       @Value("${app.review-service.port}") String reviewServicePort) {

        this.webClient = webClientBuilder.build();
        this.mapper = mapper;

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

    //See: https://howtodoinjava.com/spring-webflux/webclient-get-post-example/
    public Mono<Product> createProduct(Product body) {
        String url = productServiceUrl + "/product";
        logger.debug("Posting a new product to URL: {}", url);

        Mono<Product> productMono =  webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(body), Product.class)
                .retrieve()
                .bodyToMono(Product.class)
                .log(logger.getName(), FINE)
                .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));

        logger.debug("Created a product with productId: {}", body.getProductId());
        return productMono;
    }

    public Mono<Recommendation> createRecommendation(Recommendation body) {
        String url = recommendationServiceUrl + "/recommendation";
        logger.debug("Posting a new recommendation to URL: {}", url);

        Mono<Recommendation> recommendationMono =  webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(body), Recommendation.class)
                .retrieve()
                .bodyToMono(Recommendation.class)
                .log(logger.getName(), FINE)
                .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));

        logger.debug("Created a recommendation for productId: {}", body.getProductId());
        return recommendationMono;
    }

    public Mono<Review> createReview(Review body) {
        String url = reviewServiceUrl + "/review";
        logger.debug("Posting a new review to URL: {}", url);

        Mono<Review> reviewMono =  webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(body), Review.class)
                .retrieve()
                .bodyToMono(Review.class)
                .log(logger.getName(), FINE)
                .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));

        logger.debug("Created a review for productId: {}", body.getProductId());

        return reviewMono;
    }

    //See:
    //(1). https://www.learninjava.com/spring-webclient-get-post-put-delete-example/
    //(2) https://medium.com/@darogadibia/webclient-the-new-resttemplate-8f3608e8b049
    public Mono<Void> deleteProduct(int productId) {
        String url = productServiceUrl +  "/product/" + productId;
        logger.debug("Calling the deleteProduct API on URL: {}", url);

        Mono<Void> productMono = webClient.delete()
                .uri(url)
                .retrieve()
                .bodyToMono(Void.class)
                .log(logger.getName(), FINE)
                .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));

        logger.debug("Delete attempt for product with productId: {}", productId);

        return productMono;
    }

    public Mono<Void> deleteRecommendations(int productId) {
        String url = recommendationServiceUrl + "/recommendation?productId=" + productId;
        logger.debug("Calling the deleteRecommendations API on URL: {}", url);

        Mono<Void> recommendationMono = webClient.delete()
                .uri(url)
                .retrieve()
                .bodyToMono(Void.class)
                .log(logger.getName(), FINE)
                .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));

        logger.debug("Delete attempt a recommendation of productId: {}", productId);

        return recommendationMono;
    }

    public Mono<Void> deleteReviews(int productId) {
        String url = reviewServiceUrl + "/review?productId=" + productId;
        logger.debug("Calling the deleteReviews API on URL: {}", url);

        Mono<Void> reviewMono = webClient.delete()
                .uri(url)
                .retrieve()
                .bodyToMono(Void.class)
                .log(logger.getName(), FINE)
                .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));

        logger.debug("Delete attempt a review of productId: {}", productId);

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
}
