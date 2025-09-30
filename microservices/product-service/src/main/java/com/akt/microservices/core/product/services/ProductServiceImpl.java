package com.akt.microservices.core.product.services;

import com.akt.api.core.product.Product;
import com.akt.api.core.product.ProductService;
import com.akt.api.exceptions.InvalidInputException;
import com.akt.api.exceptions.NotFoundException;
import com.akt.microservices.core.product.persistence.ProductEntity;
import com.akt.microservices.core.product.persistence.ProductRepository;
import com.akt.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static java.util.logging.Level.FINE;

@RestController
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository repository;
    private final ProductMapper mapper;
    private final ServiceUtil serviceUtil;

    @Autowired
    public ProductServiceImpl(ProductRepository repository, ProductMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Mono<Product> createProduct(Product body) {
        logger.info("createProduct call for productId={}", body.getProductId());

        ProductEntity entity = mapper.dtoToEntity(body);
        Mono<Product> newEntity = repository.save(entity)
                .log(logger.getName(), FINE)
                .onErrorMap(
                        DuplicateKeyException.class,
                        ex -> new InvalidInputException("Duplicate key, Product Id: " + body.getProductId()))
                .map(e -> mapper.entityToDto(e));

        logger.info("createProduct: created product with productId: {}", body.getProductId());

        return newEntity;
    }

    @Override
    public Mono<Product> getProduct(int productId) {
        logger.info("getProduct call for productId={}", productId);

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        Mono<Product> entity = repository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("No product found for productId: " + productId)))
                .log(logger.getName(), FINE)
                .map(e -> mapper.entityToDto(e))
                .map(e -> setServiceAddress(e));

        logger.info("getProduct: found productId: {}", productId);

        return entity;
    }

    @Override
    public Mono<Void> deleteProduct(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        logger.debug("deleteProduct: attempt to delete an entity with productId: {}", productId);
        return repository.findByProductId(productId).log(logger.getName(), FINE).map(e -> repository.delete(e)).flatMap(e -> e);
    }

    private Product setServiceAddress(Product product) {
        product.setServiceAddress(serviceUtil.getServiceAddress());
        return product;
    }
}
