package com.akt.microservices.core.recommendation.services;

import com.akt.api.core.recommendation.Recommendation;
import com.akt.api.core.recommendation.RecommendationService;
import com.akt.api.core.review.Review;
import com.akt.api.exceptions.InvalidInputException;
import com.akt.microservices.core.recommendation.persistence.RecommendationEntity;
import com.akt.microservices.core.recommendation.persistence.RecommendationRepository;
import com.akt.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class RecommendationServiceImpl implements RecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationServiceImpl.class);

    private final RecommendationRepository repository;
    private final RecommendationMapper mapper;
    private final ServiceUtil serviceUtil;

    @Autowired
    public RecommendationServiceImpl(RecommendationRepository repository,
                                     RecommendationMapper mapper,
                                     ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public List<Recommendation> getRecommendation(int productId) {

        return null;

        /*if(productId < 1){
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        List<RecommendationEntity> entityList = repository.findByProductId(productId);
        List<Recommendation> recommendations = mapper.entityListToDtoList(entityList);
        recommendations.forEach(r -> r.setServiceAddress(serviceUtil.getServiceAddress()));

        logger.debug("getRecommendation: response size: {}", recommendations.size());;

        return recommendations;*/
    }

    @Override
    public Recommendation createRecommendation(Recommendation body) {

        return null;

        /*try{
            RecommendationEntity entity = mapper.dtoToEntity(body);
            RecommendationEntity newEntity = repository.save(entity);

            logger.debug("createReview: created a new entity: {}/{}", body.getProductId(), body.getRecommendationId());
            return mapper.entityToDto(newEntity);
        }
        catch (DataIntegrityViolationException exception){
            throw new InvalidInputException("Duplicate key, Product Id:" + body.getProductId() + ", Recommendation Id:" + body.getRecommendationId());
        }*/
    }

    @Override
    public void deleteRecommendations(int productId) {
        /*logger.debug("deleteRecommendations: deleting recommendations for product with productId: {}", productId);
        repository.deleteAll(repository.findByProductId(productId));*/
    }
}
