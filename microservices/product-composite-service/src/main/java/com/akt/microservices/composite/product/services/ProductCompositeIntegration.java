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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.springframework.http.HttpMethod.GET;

@Component
public class ProductCompositeIntegration {

    private static final Logger logger = LoggerFactory.getLogger(ProductCompositeIntegration.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;

    @Autowired
    public ProductCompositeIntegration(RestTemplate restTemplate,
       ObjectMapper mapper,
       @Value("${app.product-service.host}") String productServiceHost,
       @Value("${app.product-service.port}") String productServicePort,
       @Value("${app.recommendation-service.host}") String recommendationServiceHost,
       @Value("${app.recommendation-service.port}") String recommendationServicePort,
       @Value("${app.review-service.host}") String reviewServiceHost,
       @Value("${app.review-service.port}") String reviewServicePort) {

        this.restTemplate = restTemplate;
        this.mapper = mapper;

        productServiceUrl = "http://" + productServiceHost + ":" + productServicePort + "/product";
        recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/recommendation";
        reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review";
    }

    public Product getProduct(int productId){
        try{
            String url = productServiceUrl +  "/" + productId;

            logger.debug("Calling getProduct API on URL: {}", url);

            Product product = restTemplate.getForObject(url, Product.class);
            logger.debug("Found product with id: {}", productId);

            return product;
        } catch (HttpClientErrorException exception){
            switch (HttpStatus.resolve(exception.getStatusCode().value())){
                case NOT_FOUND -> throw new NotFoundException(getErrorMessage(exception));
                case UNPROCESSABLE_ENTITY -> throw new InvalidInputException(getErrorMessage(exception));
                default -> {
                    logger.warn("Unexpected HTTP error; {}. Rethrowing it", exception.getStatusCode());
                    logger.warn("Http errof body: {}", exception.getResponseBodyAsString());
                    throw exception;
                }
            }
        }
    }

    public List<Recommendation> getRecommendations(int productId){
        try{
            String url = recommendationServiceUrl + "?productId=" + productId;

            logger.debug("Calling getRecommedandations API on URL: {}", url);
            List<Recommendation> recommendations = restTemplate
                    .exchange(url, GET, null, new ParameterizedTypeReference<List<Recommendation>>() {})
                    .getBody();
            logger.debug("Found {} recommendations for product with id: {}", recommendations.size(), productId);
            return recommendations;
        } catch (Exception exception){
            logger.warn("Exception while requesting recommendations. Returning zero recommendations: {}", exception.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Review> getReviews(int productId) {
        try{
            String url = reviewServiceUrl + "?productId=" + productId;

            logger.debug("Calling getReviews API on URL: {}", url);
            List<Review> reviews = restTemplate
                    .exchange(url, GET, null, new ParameterizedTypeReference<List<Review>>() {})
                    .getBody();
            logger.debug("Found {} reviews for product with id: {}", reviews.size(), productId);
            return reviews;
        } catch (Exception exception){
            logger.warn("Exception while requesting reviews. Returning zero reviews: {}", exception.getMessage());
            return new ArrayList<>();
        }
    }

    public Product createProduct(Product body) {
        try {
            String url = productServiceUrl;
            logger.debug("Posting a new product to URL: {}", url);

            Product product = restTemplate.postForObject(url, body, Product.class);
            logger.debug("Created a product with id: {}", product.getProductId());

            return product;

        } catch (HttpClientErrorException exception) {
            throw handleHttpClientException(exception);
        }
    }

    public Recommendation createRecommendation(Recommendation body) {
        try {
            String url = recommendationServiceUrl;
            logger.debug("Posting a new recommendation to URL: {}", url);

            Recommendation recommendation = restTemplate.postForObject(url, body, Recommendation.class);
            logger.debug("Created a recommendation with id: {}", recommendation.getProductId());

            return recommendation;
        } catch (HttpClientErrorException exception) {
            throw handleHttpClientException(exception);
        }
    }

    public Review createReview(Review body) {
        try {
            String url = reviewServiceUrl;
            logger.debug("Posting a new review to URL: {}", url);

            Review review = restTemplate.postForObject(url, body, Review.class);
            logger.debug("Created a Review with id: {}", review.getProductId());

            return review;
        } catch (HttpClientErrorException exception) {
            throw handleHttpClientException(exception);
        }
    }

    public void deleteProduct(int productId) {
        try {
            String url = productServiceUrl + "/" + productId;
            logger.debug("Calling the deleteProduct API on URL: {}", url);

            restTemplate.delete(url);
        } catch (HttpClientErrorException exception) {
            throw handleHttpClientException(exception);
        }
    }

    public void deleteRecommendations(int productId) {
        try {
            String url = recommendationServiceUrl + "?productId=" + productId;
            logger.debug("Calling the deleteRecommendations API on URL: {}", url);

            restTemplate.delete(url);
        } catch (HttpClientErrorException exception) {
            throw handleHttpClientException(exception);
        }
    }

    public void deleteReviews(int productId) {
        try {
            String url = reviewServiceUrl + "?productId=" + productId;
            logger.debug("Calling the deleteReviews API on URL: {}", url);

            restTemplate.delete(url);
        } catch (HttpClientErrorException exception) {
            throw handleHttpClientException(exception);
        }
    }

    private String getErrorMessage(HttpClientErrorException exception){
        try{
            return mapper.readValue(exception.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioException){
            return exception.getMessage();
        }
    }

    private RuntimeException handleHttpClientException(HttpClientErrorException exception) {
        switch (Objects.requireNonNull(HttpStatus.resolve(exception.getStatusCode().value()))) {

            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(exception));

            case UNPROCESSABLE_ENTITY:
                return new InvalidInputException(getErrorMessage(exception));

            default:
                logger.warn("Got an unexpected HTTP error: {}, will rethrow it", exception.getStatusCode());
                logger.warn("Error body: {}", exception.getResponseBodyAsString());
                return exception;
        }
    }
}
