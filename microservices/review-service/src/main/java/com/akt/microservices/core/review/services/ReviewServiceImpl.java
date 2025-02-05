package com.akt.microservices.core.review.services;

import com.akt.api.core.recommendation.Recommendation;
import com.akt.api.core.recommendation.RecommendationService;
import com.akt.api.core.review.Review;
import com.akt.api.core.review.ReviewService;
import com.akt.api.exceptions.InvalidInputException;
import com.akt.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ReviewServiceImpl implements ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewServiceImpl.class);
    private final ServiceUtil serviceUtil;

    @Autowired
    public ReviewServiceImpl(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public List<Review> getReview(int productId) {
        if(productId < 1){
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        if(productId == 213){
            logger.debug("No reviews found for productId: {}", productId);
            return new ArrayList<>();
        }

        List<Review> reviews = new ArrayList<>();
        reviews.add(new Review(productId, 1, "Author 1", "Subject 1", "Content1", serviceUtil.getServiceAddress()));
        reviews.add(new Review(productId, 2, "Author 2", "Subject 2", "Content2", serviceUtil.getServiceAddress()));
        reviews.add(new Review(productId, 3, "Author 3", "Subject 3", "Content2", serviceUtil.getServiceAddress()));

        logger.debug("/reviews response size: {}", reviews.size());

        return reviews;
    }
}
