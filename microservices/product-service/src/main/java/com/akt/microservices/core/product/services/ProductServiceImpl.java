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

@RestController
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository repository;
    private final ProductMapper mapper;
    private final ServiceUtil serviceUtil;

    public ProductServiceImpl(ProductRepository repository, ProductMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Product createProduct(Product body) {
        logger.info("createProduct call for productId={}", body.getProductId());

        try {
            ProductEntity entity = mapper.dtoToEntity(body);
            ProductEntity newEntity = repository.save(entity);

            logger.info("createProduct: entity created for productId: {}", body.getProductId());
            return mapper.entityToDto(newEntity);

        } catch (DuplicateKeyException exception) {
            throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId());
        }
    }

    @Override
    public Product getProduct(int productId) {
        logger.info("getProduct call for productId={}", productId);

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        ProductEntity entity = repository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("No product found for productId: " + productId));

        Product product = mapper.entityToDto(entity);
        product.setServiceAddress(serviceUtil.getServiceAddress());

        logger.info("getProduct: found productId: {}", product.getProductId());

        return product;
    }

    @Override
    public void deleteProduct(int productId) {
        logger.info("deleteProduct: deleting product with productId: {}", productId);
        repository.findByProductId(productId).ifPresent(repository::delete);
    }
}
