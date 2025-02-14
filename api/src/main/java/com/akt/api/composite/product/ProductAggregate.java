package com.akt.api.composite.product;

import java.util.List;

public class ProductAggregate {
    private final int productId;
    private final String name;
    private final int weight;
    private final List<RecommendationSummary> recommendations;
    private final List<ReviewSummary> reviews;
    private final ServiceAddresses serviceAddresses;

    public ProductAggregate() {
        this.productId = 0;
        this.name = null;
        this.weight = 0;
        this.recommendations = null;
        this.reviews = null;
        this.serviceAddresses = null;
    }

    public ProductAggregate(int productId,
                            String name,
                            int weight,
                            List<RecommendationSummary> recommendationSummaries,
                            List<ReviewSummary> reviewSummaries,
                            ServiceAddresses serviceAddresses) {
        this.productId = productId;
        this.name = name;
        this.weight = weight;
        this.recommendations = recommendationSummaries;
        this.reviews = reviewSummaries;
        this.serviceAddresses = serviceAddresses;
    }

    public int getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }

    public List<RecommendationSummary> getRecommendations() {
        return recommendations;
    }

    public List<ReviewSummary> getReviews() {
        return reviews;
    }

    public ServiceAddresses getServiceAddresses() {
        return serviceAddresses;
    }
}
