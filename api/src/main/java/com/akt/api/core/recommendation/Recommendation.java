package com.akt.api.core.recommendation;

public class Recommendation {
    private int productId;
    private int recommendationId;
    private String author;
    private int rating;
    private String content;
    private String serviceAddress;

    public Recommendation() {
        productId = 0;
        recommendationId = 0;
        author = null;
        rating = 0;
        content = null;
        serviceAddress = null;
    }

    public Recommendation(
            int productId,
            int recommendationId,
            String author,
            int rating,
            String content,
            String serviceAddress) {

        this.productId = productId;
        this.recommendationId = recommendationId;
        this.author = author;
        this.rating = rating;
        this.content = content;
        this.serviceAddress = serviceAddress;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getRecommendationId() {
        return recommendationId;
    }

    public void setRecommendationId(int recommendationId) {
        this.recommendationId = recommendationId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }
}