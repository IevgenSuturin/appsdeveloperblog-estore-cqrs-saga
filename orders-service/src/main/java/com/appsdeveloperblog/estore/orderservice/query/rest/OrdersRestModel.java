package com.appsdeveloperblog.estore.orderservice.query.rest;

import com.appsdeveloperblog.estore.orderservice.core.model.OrderStatus;
import lombok.Data;

@Data
public class OrdersRestModel {
    public String orderId;
    private String userId;
    private String productId;
    private int quantity;
    private String addressId;
    private OrderStatus orderStatus;
}
