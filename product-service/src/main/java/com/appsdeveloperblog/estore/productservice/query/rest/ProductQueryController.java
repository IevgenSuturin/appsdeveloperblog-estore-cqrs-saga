package com.appsdeveloperblog.estore.productservice.query.rest;

import com.appsdeveloperblog.estore.productservice.query.FindProductsQuery;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/products")
public class ProductQueryController {

    @Autowired
    QueryGateway queryGateway;

    @GetMapping
    public ResponseEntity<List<ProductRestModel>> getProducts() {

        FindProductsQuery findProductsQuery = new FindProductsQuery();
        List<ProductRestModel> products = queryGateway
                .query(findProductsQuery, ResponseTypes.multipleInstancesOf(ProductRestModel.class))
                .join();

        return ResponseEntity.of(Optional.of(products));
    }
}
