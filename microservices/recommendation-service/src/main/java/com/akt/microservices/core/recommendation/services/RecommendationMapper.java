package com.akt.microservices.core.recommendation.services;

import com.akt.api.core.recommendation.Recommendation;
import com.akt.microservices.core.recommendation.persistence.RecommendationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecommendationMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true),
    })
    RecommendationEntity dtoToEntity(Recommendation dto);

    @Mappings({
            @Mapping(target = "serviceAddress", ignore = true)
    })
    Recommendation entityToDto(RecommendationEntity entity);

    List<RecommendationEntity> dtoListToEntityList(List<Recommendation> dtoList);

    List<Recommendation> entityListToDtoList(List<RecommendationEntity> entityList);
}
