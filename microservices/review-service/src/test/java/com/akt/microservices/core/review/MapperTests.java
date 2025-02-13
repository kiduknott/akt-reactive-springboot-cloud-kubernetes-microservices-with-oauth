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

    Review dto = new Review(1, 2, "a", "s", "C", "adr");

    ReviewEntity entity = mapper.apiToEntity(dto);

    assertEquals(dto.getProductId(), entity.getProductId());
    assertEquals(dto.getReviewId(), entity.getReviewId());
    assertEquals(dto.getAuthor(), entity.getAuthor());
    assertEquals(dto.getSubject(), entity.getSubject());
    assertEquals(dto.getContent(), entity.getContent());

    Review newDto = mapper.entityToApi(entity);

    assertEquals(dto.getProductId(), newDto.getProductId());
    assertEquals(dto.getReviewId(), newDto.getReviewId());
    assertEquals(dto.getAuthor(), newDto.getAuthor());
    assertEquals(dto.getSubject(), newDto.getSubject());
    assertEquals(dto.getContent(), newDto.getContent());
    assertNull(newDto.getServiceAddress());
  }

  @Test
  void mapperListTests() {

    assertNotNull(mapper);

    Review dto = new Review(1, 2, "a", "s", "C", "adr");
    List<Review> dtoList = Collections.singletonList(dto);

    List<ReviewEntity> entityList = mapper.apiListToEntityList(dtoList);
    assertEquals(dtoList.size(), entityList.size());

    ReviewEntity entity = entityList.get(0);

    assertEquals(dto.getProductId(), entity.getProductId());
    assertEquals(dto.getReviewId(), entity.getReviewId());
    assertEquals(dto.getAuthor(), entity.getAuthor());
    assertEquals(dto.getSubject(), entity.getSubject());
    assertEquals(dto.getContent(), entity.getContent());

    List<Review> newDtoList = mapper.entityListToApiList(entityList);
    assertEquals(dtoList.size(), newDtoList.size());

    Review newDto = newDtoList.get(0);

    assertEquals(dto.getProductId(), newDto.getProductId());
    assertEquals(dto.getReviewId(), newDto.getReviewId());
    assertEquals(dto.getAuthor(), newDto.getAuthor());
    assertEquals(dto.getSubject(), newDto.getSubject());
    assertEquals(dto.getContent(), newDto.getContent());
    assertNull(newDto.getServiceAddress());
  }
}
