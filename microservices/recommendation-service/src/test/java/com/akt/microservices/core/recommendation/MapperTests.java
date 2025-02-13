package com.akt.microservices.core.recommendation;

import com.akt.api.core.recommendation.Recommendation;
import com.akt.microservices.core.recommendation.persistence.RecommendationEntity;
import com.akt.microservices.core.recommendation.services.RecommendationMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MapperTests {
    private RecommendationMapper mapper = Mappers.getMapper(RecommendationMapper.class);

    @Test
    void mapperTests() {

        assertNotNull(mapper);

        Recommendation dto = new Recommendation(1, 1, "a1", 1, "c1", "addr1");

        RecommendationEntity entity = mapper.dtoToEntity(dto);

        assertEquals(dto.getProductId(), entity.getProductId());
        assertEquals(dto.getRecommendationId(), entity.getRecommendationId());
        assertEquals(dto.getAuthor(), entity.getAuthor());
        assertEquals(dto.getRating(), entity.getRating());
        assertEquals(dto.getContent(), entity.getContent());

        Recommendation newDto = mapper.entityToDto(entity);

        assertEquals(dto.getProductId(), newDto.getProductId());
        assertEquals(dto.getRecommendationId(), newDto.getRecommendationId());
        assertEquals(dto.getAuthor(), newDto.getAuthor());
        assertEquals(dto.getRating(), newDto.getRating());
        assertEquals(dto.getContent(), newDto.getContent());
        assertNull(newDto.getServiceAddress());
    }

    @Test
    void mapperListTests(){

        assertNotNull(mapper);

        Recommendation dto = new Recommendation(2, 2, "a2", 2, "c2", "addr2");
        List<Recommendation> dtoList = Collections.singletonList(dto);

        List<RecommendationEntity> entityList = mapper.dtoListToEntityList(dtoList);
        assertEquals(dtoList.size(), entityList.size());

        RecommendationEntity entity = entityList.get(0);

        assertEquals(dto.getProductId(), entity.getProductId());
        assertEquals(dto.getRecommendationId(), entity.getRecommendationId());
        assertEquals(dto.getAuthor(), entity.getAuthor());
        assertEquals(dto.getRating(), entity.getRating());
        assertEquals(dto.getContent(), entity.getContent());

        List<Recommendation> newDtoList = mapper.entityListToDtoList(entityList);
        assertEquals(dtoList.size(), entityList.size());

        Recommendation newDto = newDtoList.get(0);

        assertEquals(dto.getProductId(), newDto.getProductId());
        assertEquals(dto.getRecommendationId(), newDto.getRecommendationId());
        assertEquals(dto.getAuthor(), newDto.getAuthor());
        assertEquals(dto.getRating(), newDto.getRating());
        assertEquals(dto.getContent(), newDto.getContent());
        assertNull(newDto.getServiceAddress());

    }
}
