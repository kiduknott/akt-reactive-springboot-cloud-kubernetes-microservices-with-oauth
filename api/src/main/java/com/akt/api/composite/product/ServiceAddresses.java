package com.akt.api.composite.product;

public class ServiceAddresses {

    private final String compositeAddress;
    private final String productAddress;
    private final String reviewAddress;
    private final String recommendationAddress;

    public ServiceAddresses() {
        this.compositeAddress = null;
        this.productAddress = null;
        this.reviewAddress = null;
        this.recommendationAddress = null;
    }

    public ServiceAddresses(String compositeAddress,
                            String productAddress,
                            String reviewAddress,
                            String recommendationAddress) {
        this.compositeAddress = compositeAddress;
        this.productAddress = productAddress;
        this.reviewAddress = reviewAddress;
        this.recommendationAddress = recommendationAddress;
    }

    public String getCompositeAddress() {
        return compositeAddress;
    }

    public String getProductAddress() {
        return productAddress;
    }

    public String getReviewAddress() {
        return reviewAddress;
    }

    public String getRecommendationAddress() {
        return recommendationAddress;
    }
}
