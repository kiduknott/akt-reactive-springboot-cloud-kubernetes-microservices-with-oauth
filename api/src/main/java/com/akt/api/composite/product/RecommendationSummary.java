package com.akt.api.composite.product;

public class RecommendationSummary {

    private final int recommendationId;
    private final String author;
    private final int rating;

    public RecommendationSummary(int recommendationId, String author, int rating) {
        this.recommendationId = recommendationId;
        this.author = author;
        this.rating = rating;
    }

    public int getRecommendationId() {
        return recommendationId;
    }

    public String getAuthor() {
        return author;
    }

    public int getRating() {
        return rating;
    }
}
