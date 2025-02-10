package com.akt.microservices.core.review.services;

import com.akt.api.core.recommendation.Recommendation;
import com.akt.api.core.recommendation.RecommendationService;
import com.akt.api.core.review.Review;
import com.akt.api.core.review.ReviewService;
import com.akt.api.exceptions.InvalidInputException;
import com.akt.microservices.core.review.persistence.ReviewEntity;
import com.akt.microservices.core.review.persistence.ReviewRepository;
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

    private final ReviewRepository repository;
    private final ReviewMapper mapper;
    private final ServiceUtil serviceUtil;

    @Autowired
    public ReviewServiceImpl(ReviewRepository repository, ReviewMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public List<Review> getReview(int productId) {
        if(productId < 1){
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        List<ReviewEntity> entityList = repository.findByProductId(productId);
        List<Review> reviews = mapper.entityListToApiList(entityList);
        reviews.forEach(r -> r.setServiceAddress(serviceUtil.getServiceAddress()));

        logger.debug("/reviews response size: {}", reviews.size());

        return reviews;
    }
}
