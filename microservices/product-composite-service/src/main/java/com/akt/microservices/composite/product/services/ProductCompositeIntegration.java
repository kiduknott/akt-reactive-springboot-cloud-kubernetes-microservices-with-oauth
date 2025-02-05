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
        this.productServiceUrl = "http://" + productServiceHost + ":" + productServicePort + "/product/";
        this.recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/recommendation?productId=";
        this.reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review?productId=";
    }

    public Product getProduct(int productId){
        try{
            String url = productServiceUrl + productId;

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
            String url = recommendationServiceUrl + productId;

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
            String url = reviewServiceUrl + productId;

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

    private String getErrorMessage(HttpClientErrorException exception){
        try{
            return mapper.readValue(exception.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioException){
            return exception.getMessage();
        }
    }
}
