package com.appsdeveloperblog.estore.productservice.query;

import com.appsdeveloperblog.estore.core.events.ProductReservationCanceledEvent;
import com.appsdeveloperblog.estore.core.events.ProductReservedEvent;
import com.appsdeveloperblog.estore.productservice.core.data.ProductEntity;
import com.appsdeveloperblog.estore.productservice.core.data.ProductsRepository;
import com.appsdeveloperblog.estore.productservice.core.events.ProductCreatedEvent;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.axonframework.messaging.interceptors.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("product-group")
public class ProductEventsHandler {

    private static final Logger logger = LoggerFactory.getLogger(ProductEventsHandler.class);

    private final ProductsRepository productsRepository;

    public ProductEventsHandler(ProductsRepository productsRepository) {
        this.productsRepository = productsRepository;
    }

    @ExceptionHandler(resultType = Exception.class)
    public void handle(Exception exception) throws Exception{
        throw exception;
    }

    @ExceptionHandler(resultType = IllegalArgumentException.class)
    public void handle(IllegalArgumentException exception) {
        // Log error message
    }

    @EventHandler
    public void on(ProductCreatedEvent event) {
        ProductEntity productEntity = new ProductEntity();
        BeanUtils.copyProperties(event, productEntity);

        productsRepository.save(productEntity);
    }

    @EventHandler
    public void on(ProductReservedEvent event) {
        ProductEntity productEntity = productsRepository.findByProductId(event.getProductId());
        productEntity.setQuantity(productEntity.getQuantity() - event.getQuantity());
        productsRepository.save(productEntity);

        logger.info("ProductReservedEvent is called for productId: " + event.getProductId());
    }

    @EventHandler
    public void on(ProductReservationCanceledEvent event) {
        ProductEntity productEntity = productsRepository.findByProductId(event.getProductId());
        productEntity.setQuantity(productEntity.getQuantity() + event.getQuantity());
        productsRepository.save(productEntity);

        logger.info("ProductReservationCanceledEvent is called for productId: " + event.getProductId());
    }

    @ResetHandler
    public void reset() {
        productsRepository.deleteAll();
    }
}
