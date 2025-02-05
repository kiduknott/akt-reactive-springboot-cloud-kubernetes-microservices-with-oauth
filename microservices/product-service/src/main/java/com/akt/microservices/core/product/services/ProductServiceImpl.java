package com.akt.microservices.core.product.services;

import com.akt.api.core.product.Product;
import com.akt.api.core.product.ProductService;
import com.akt.api.exceptions.InvalidInputException;
import com.akt.api.exceptions.NotFoundException;
import com.akt.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductServiceImpl implements ProductService {
    private final ServiceUtil serviceUtil;

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    public ProductServiceImpl(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Product getProduct(int productId) {
        logger.debug("/getProduct call for productId={}", productId);

        if(productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        if(productId == 123){
            throw new NotFoundException("No product found for productId: " + productId);
        }

        return new Product(productId, "name-" + productId, 123, serviceUtil.getServiceAddress());
    }
}
