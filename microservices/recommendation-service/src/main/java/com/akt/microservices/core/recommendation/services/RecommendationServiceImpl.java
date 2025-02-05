package com.akt.microservices.core.recommendation.services;

import com.akt.api.core.recommendation.Recommendation;
import com.akt.api.core.recommendation.RecommendationService;
import com.akt.api.exceptions.InvalidInputException;
import com.akt.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class RecommendationServiceImpl implements RecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationServiceImpl.class);
    private final ServiceUtil serviceUtil;

    @Autowired
    public RecommendationServiceImpl(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public List<Recommendation> getRecommendation(int productId) {
        if(productId < 1){
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        if(productId == 113){
            logger.debug("No recommendations found for productId: {}", productId);
            return new ArrayList<>();
        }

        List<Recommendation> recommendations = new ArrayList<Recommendation>();
        recommendations.add(new Recommendation(productId, 1, "Author 1", 1, "Content1", serviceUtil.getServiceAddress()));
        recommendations.add(new Recommendation(productId, 2, "Author 2", 2, "Content2", serviceUtil.getServiceAddress()));
        recommendations.add(new Recommendation(productId, 3, "Author 3", 3, "Content2", serviceUtil.getServiceAddress()));

        logger.debug("/recommendations response size: {}", recommendations.size());

        return recommendations;
    }
}
