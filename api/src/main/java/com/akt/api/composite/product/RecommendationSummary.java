package com.akt.api.composite.product;

public class RecommendationSummary {

    private final int recommendationId;
    private final String author;
    private final int rating;
    private final String content;

    public RecommendationSummary() {
        this.recommendationId = 0;
        this.author = null;
        this.rating = 0;
        this.content = null;
    }

    public RecommendationSummary(int recommendationId, String author, int rating, String content) {
        this.recommendationId = recommendationId;
        this.author = author;
        this.rating = rating;
        this.content = content;
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

    public String getContent() {
        return content;
    }
}
