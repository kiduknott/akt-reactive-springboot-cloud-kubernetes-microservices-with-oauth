package com.akt.microservices.core.product.services;

import com.akt.api.core.product.Product;
import com.akt.microservices.core.product.persistence.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true),
    })
    ProductEntity dtoToEntity(Product dto);

    @Mappings({
            @Mapping(target = "serviceAddress", ignore = true)
    })
    Product entityToDto(ProductEntity entity);

}
