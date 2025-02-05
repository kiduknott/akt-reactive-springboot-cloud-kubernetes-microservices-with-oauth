package com.akt.api.core.review;

import java.util.List;

import com.akt.api.core.recommendation.Recommendation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface ReviewService {
    @GetMapping(
            value = "/review",
            produces = "application/json")
    List<Review> getReview(@RequestParam(value = "productId", required = true) int productId);
}