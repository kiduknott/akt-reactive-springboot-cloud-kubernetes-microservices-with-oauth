package com.akt.microservices.core.review;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;

import com.akt.api.core.review.Review;
import com.akt.microservices.core.review.persistence.ReviewEntity;
import com.akt.microservices.core.review.services.ReviewMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;


class MapperTests {

  private ReviewMapper mapper = Mappers.getMapper(ReviewMapper.class);

  @Test
  void mapperTests() {

    assertNotNull(mapper);

    Review initialReview = new Review(1, 2, "a", "s", "C", "adr");

    ReviewEntity entity = mapper.apiToEntity(initialReview);

    assertEquals(initialReview.getProductId(), entity.getProductId());
    assertEquals(initialReview.getReviewId(), entity.getReviewId());
    assertEquals(initialReview.getAuthor(), entity.getAuthor());
    assertEquals(initialReview.getSubject(), entity.getSubject());
    assertEquals(initialReview.getContent(), entity.getContent());

    Review transformedReview = mapper.entityToApi(entity);

    assertEquals(initialReview.getProductId(), transformedReview.getProductId());
    assertEquals(initialReview.getReviewId(), transformedReview.getReviewId());
    assertEquals(initialReview.getAuthor(), transformedReview.getAuthor());
    assertEquals(initialReview.getSubject(), transformedReview.getSubject());
    assertEquals(initialReview.getContent(), transformedReview.getContent());
    assertNull(transformedReview.getServiceAddress());
  }

  @Test
  void mapperListTests() {

    assertNotNull(mapper);

    Review initialReview = new Review(1, 2, "a", "s", "C", "adr");
    List<Review> initialReviewList = Collections.singletonList(initialReview);

    List<ReviewEntity> entityList = mapper.apiListToEntityList(initialReviewList);
    assertEquals(initialReviewList.size(), entityList.size());

    ReviewEntity entity = entityList.get(0);

    assertEquals(initialReview.getProductId(), entity.getProductId());
    assertEquals(initialReview.getReviewId(), entity.getReviewId());
    assertEquals(initialReview.getAuthor(), entity.getAuthor());
    assertEquals(initialReview.getSubject(), entity.getSubject());
    assertEquals(initialReview.getContent(), entity.getContent());

    List<Review> transformedReviewList = mapper.entityListToApiList(entityList);
    assertEquals(initialReviewList.size(), transformedReviewList.size());

    Review transformedReview = transformedReviewList.get(0);

    assertEquals(initialReview.getProductId(), transformedReview.getProductId());
    assertEquals(initialReview.getReviewId(), transformedReview.getReviewId());
    assertEquals(initialReview.getAuthor(), transformedReview.getAuthor());
    assertEquals(initialReview.getSubject(), transformedReview.getSubject());
    assertEquals(initialReview.getContent(), transformedReview.getContent());
    assertNull(transformedReview.getServiceAddress());
  }
}
