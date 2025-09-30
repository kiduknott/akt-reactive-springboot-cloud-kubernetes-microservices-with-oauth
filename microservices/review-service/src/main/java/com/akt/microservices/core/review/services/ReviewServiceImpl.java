package com.akt.microservices.core.review.services;

import com.akt.api.core.review.Review;
import com.akt.api.core.review.ReviewService;
import com.akt.api.exceptions.InvalidInputException;
import com.akt.microservices.core.review.persistence.ReviewEntity;
import com.akt.microservices.core.review.persistence.ReviewRepository;
import com.akt.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.List;

import static java.util.logging.Level.FINE;

@RestController
public class ReviewServiceImpl implements ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewRepository repository;
    private final ReviewMapper mapper;
    private final ServiceUtil serviceUtil;
    private final Scheduler jdbcScheduler;

    @Autowired
    public ReviewServiceImpl(ReviewRepository repository,
                             ReviewMapper mapper,
                             ServiceUtil serviceUtil,
                             @Qualifier("jdbcScheduler") Scheduler jdbcScheduler) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
        this.jdbcScheduler = jdbcScheduler;
    }

    @Override
    public Flux<Review> getReviews(int productId) {
        if(productId < 1){
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        return Mono.fromCallable(() -> internalGetReviews(productId))
                .flatMapMany(Flux::fromIterable)
                .log(logger.getName(), FINE)
                .subscribeOn(jdbcScheduler);
    }

    @Override
    public Mono<Review> createReview(Review body) {
        int productId = body.getProductId();

        if(productId < 1){
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        return Mono.fromCallable(() -> internalCreateReview(body))
                .subscribeOn(jdbcScheduler);
    }

    @Override
    public Mono<Void> deleteReviews(int productId) {
        if(productId < 1){
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        return Mono.fromRunnable(() -> internalDeleteReviews(productId))
                .subscribeOn(jdbcScheduler).then();
    }

    private List<Review> internalGetReviews(int productId) {
        List<ReviewEntity> entityList = repository.findByProductId(productId);
        List<Review> reviews = mapper.entityListToApiList(entityList);
        reviews.forEach(r -> r.setServiceAddress(serviceUtil.getServiceAddress()));

        logger.debug("getReview: response size: {}", reviews.size());

        return reviews;
    }

    private Review internalCreateReview(Review body) {
        try{
            ReviewEntity entity = mapper.apiToEntity(body);
            ReviewEntity newEntity = repository.save(entity);

            logger.debug("createReview: created a new entity: {}/{}", body.getProductId(), body.getReviewId());
            return mapper.entityToApi(newEntity);
        }
        catch (DataIntegrityViolationException exception){
            throw new InvalidInputException("Duplicate key, Product Id:" + body.getProductId() + ", Review Id:" + body.getReviewId());
        }
    }

    private void internalDeleteReviews(int productId) {
        logger.debug("deleteReviews: deleting reviews for product with productId: {}", productId);
        repository.deleteAll(repository.findByProductId(productId));
    }
}
