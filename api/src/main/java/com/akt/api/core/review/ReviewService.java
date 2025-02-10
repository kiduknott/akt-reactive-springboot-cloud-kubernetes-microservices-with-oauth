package com.akt.api.core.review;

import java.util.List;

import com.akt.api.core.recommendation.Recommendation;
import org.springframework.web.bind.annotation.*;


public interface ReviewService {

    /**
     * Sample usage: "curl $HOST:$PORT/review?productId=1".
     *
     * @param productId Id of the product
     * @return the reviews of the product
     */
    @GetMapping(
            value = "/review",
            produces = "application/json")
    List<Review> getReview(@RequestParam(value = "productId", required = true) int productId);

    /**
     * Sample usage, see below.
     *
     * curl -X POST $HOST:$PORT/review \
     *   -H "Content-Type: application/json" --data \
     *   '{"productId":123,"reviewId":456,"author":"me","subject":"the subject","content":"some content"}'
     *
     * @param body A JSON representation of the review to be created
     * @return A JSON representation of the newly created review
     */
    @PostMapping(
            value    = "/review",
            consumes = "application/json",
            produces = "application/json")
    Review createReview(@RequestBody Review body);

    /**
     * Sample usage: "curl -X DELETE $HOST:$PORT/review?productId=1".
     *
     * @param productId Id of the product
     */
    @DeleteMapping(value = "/review")
    void deleteReviews(@RequestParam(value = "productId", required = true) int productId);
}