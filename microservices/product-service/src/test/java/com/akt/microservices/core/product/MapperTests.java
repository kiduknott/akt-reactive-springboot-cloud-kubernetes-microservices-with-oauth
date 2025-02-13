package com.akt.microservices.core.product;

import com.akt.api.core.product.Product;
import com.akt.microservices.core.product.persistence.ProductEntity;
import com.akt.microservices.core.product.services.ProductMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

public class MapperTests {
    private ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void mapperTests() {

        assertNotNull(mapper);

        Product dto = new Product(1, "n1", 2, "addr1");

        ProductEntity entity = mapper.dtoToEntity(dto);

        assertEquals(dto.getProductId(), entity.getProductId());
        assertEquals(dto.getName(), entity.getName());
        assertEquals(dto.getWeight(), entity.getWeight());

        Product newDto = mapper.entityToDto(entity);

        assertEquals(dto.getProductId(), newDto.getProductId());
        assertEquals(dto.getName(), newDto.getName());
        assertEquals(dto.getWeight(), newDto.getWeight());
        assertNull(newDto.getServiceAddress());
    }
}
