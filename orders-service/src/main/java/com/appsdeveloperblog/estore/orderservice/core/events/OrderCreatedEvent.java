package com.appsdeveloperblog.estore.orderservice.core.events;

import com.appsdeveloperblog.estore.orderservice.core.model.OrderStatus;
import lombok.Data;

@Data
public class OrderCreatedEvent {
    private String orderId;
    private String productId;
    private String userId;
    private int quantity;
    private String addressId;
    private OrderStatus orderStatus;
}
