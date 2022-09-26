package com.appsdeveloperblog.estore.orderservice.core.events;

import com.appsdeveloperblog.estore.orderservice.core.model.OrderStatus;
import lombok.Value;

@Value
public class OrderRejectedEvent {
    private final String orderId;
    private final String reason;
    private final OrderStatus orderStatus = OrderStatus.REJECTED;
}
