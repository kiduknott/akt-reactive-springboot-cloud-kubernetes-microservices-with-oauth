package com.akt.microservices.composite.product.services;

import com.akt.api.composite.product.*;
import com.akt.api.core.product.Product;
import com.akt.api.core.recommendation.Recommendation;
import com.akt.api.core.review.Review;
import com.akt.api.exceptions.NotFoundException;
import com.akt.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {

    private static final Logger logger = LoggerFactory.getLogger(ProductCompositeServiceImpl.class);

    private final ServiceUtil serviceUtil;
    private ProductCompositeIntegration productCompositeIntegration;

    @Autowired
    public ProductCompositeServiceImpl(ServiceUtil serviceUtil,
                                       ProductCompositeIntegration productCompositeIntegration) {
        this.serviceUtil = serviceUtil;
        this.productCompositeIntegration = productCompositeIntegration;
    }

    @Override
    public ProductAggregate getProduct(int productId) {
        logger.debug("getProduct: getting composite aggregate for productId: {}", productId);

        Product product = productCompositeIntegration.getProduct(productId);

        if(product == null) {
            throw new NotFoundException("No product found for productId: " + productId);
        }

        List<Recommendation> recommendations = productCompositeIntegration.getRecommendations(productId);
        List<Review> reviews = productCompositeIntegration.getReviews(productId);

        return createProductAggregate(product, recommendations, reviews, serviceUtil.getServiceAddress());
    }

    @Override
    public void createProduct(ProductAggregate body) {

        logger.debug("createCompositeProduct: creating a new composite entity for productId: {}", body.getProductId());

        try {
            Product product = new Product(body.getProductId(), body.getName(), body.getWeight(), null);
            productCompositeIntegration.createProduct(product);

            if (body.getRecommendations() != null) {
                body.getRecommendations().forEach(r -> {
                    Recommendation recommendation = new Recommendation(body.getProductId(), r.getRecommendationId(), r.getAuthor(), r.getRating(), r.getContent(), null);
                    productCompositeIntegration.createRecommendation(recommendation);
                });
            }

            if (body.getReviews() != null) {
                body.getReviews().forEach(r -> {
                    Review review = new Review(body.getProductId(), r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent(), null);
                    productCompositeIntegration.createReview(review);
                });
            }

            logger.debug("createCompositeProduct: composite entities created for productId: {}", body.getProductId());

        } catch (RuntimeException exception) {
            logger.warn("createCompositeProduct failed", exception);
            throw exception;
        }
    }

    @Override
    public void deleteProduct(int productId) {
        logger.debug("deleteCompositeProduct: Deleting a product aggregate for productId: {}", productId);

        productCompositeIntegration.deleteProduct(productId);
        productCompositeIntegration.deleteRecommendations(productId);
        productCompositeIntegration.deleteReviews(productId);

        logger.debug("deleteCompositeProduct: aggregate entities deleted for productId: {}", productId);
    }

    private ProductAggregate createProductAggregate(Product product,
                                                    List<Recommendation> recommendations,
                                                    List<Review> reviews,
                                                    String serviceAddress) {
        // Product information
        int productId = product.getProductId();
        String name = product.getName();
        int weight = product.getWeight();

        // Recommendations
        List<RecommendationSummary> recommendationSummaries =
                (recommendations == null) ? null : recommendations.stream()
                        .map(r -> new RecommendationSummary(r.getRecommendationId(),
                                r.getAuthor(), r.getRating(), r.getContent()))
                        .collect(Collectors.toList());

        // Reviews
        List<ReviewSummary> reviewSummaries =
                (reviews == null) ? null : reviews.stream()
                        .map(r -> new ReviewSummary(r.getReviewId(),
                                r.getAuthor(), r.getSubject(), r.getContent()))
                        .collect(Collectors.toList());

        // Service Addresses
        String productAddress = product.getServiceAddress();
        String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";
        String recommendationAddress = (recommendations != null && recommendations.size() > 0) ? recommendations.get(0).getServiceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress,
                productAddress, reviewAddress, recommendationAddress);

        return new ProductAggregate(productId, name, weight, recommendationSummaries, reviewSummaries, serviceAddresses);
    }
}
