package com.akt.microservices.core.product.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface PropertyRepository extends PagingAndSortingRepository<ProductEntity, String>,
        CrudRepository<ProductEntity, String> {
    Optional<ProductEntity> findByProductId(int productId);
}
