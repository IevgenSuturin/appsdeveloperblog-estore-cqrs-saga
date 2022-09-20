package com.appsdeveloperblog.estore.orderservice.query.rest;

import com.appsdeveloperblog.estore.orderservice.query.FindOrdersQuery;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/orders")
public class OrdersQueryController {

    private final QueryGateway queryGateway;

    public OrdersQueryController(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    @GetMapping
    public ResponseEntity<List<OrdersRestModel>> getOrders() {
        FindOrdersQuery findOrdersQuery = new FindOrdersQuery();

        List<OrdersRestModel> orders = queryGateway
                .query(findOrdersQuery, ResponseTypes.multipleInstancesOf(OrdersRestModel.class))
                .join();

        return ResponseEntity.of(Optional.of(orders));
    }
}
