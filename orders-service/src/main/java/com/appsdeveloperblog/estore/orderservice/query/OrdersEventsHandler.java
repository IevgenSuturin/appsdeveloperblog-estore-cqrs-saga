package com.appsdeveloperblog.estore.orderservice.query;

import com.appsdeveloperblog.estore.orderservice.core.data.OrderEntity;
import com.appsdeveloperblog.estore.orderservice.core.data.OrdersRepository;
import com.appsdeveloperblog.estore.orderservice.core.events.OrderApprovedEvent;
import com.appsdeveloperblog.estore.orderservice.core.events.OrderCreatedEvent;
import com.appsdeveloperblog.estore.orderservice.core.events.OrderRejectedEvent;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.interceptors.ExceptionHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("order-group")
public class OrdersEventsHandler {

    private final OrdersRepository ordersRepository;

    public OrdersEventsHandler(OrdersRepository ordersRepository) {
        this.ordersRepository = ordersRepository;
    }

    @ExceptionHandler(resultType = Exception.class)
    public void handler(Exception exception) throws Exception {
        throw exception;
    }

    @EventHandler
    public void on(OrderCreatedEvent event) {

        OrderEntity orderEntity = new OrderEntity();
        BeanUtils.copyProperties(event, orderEntity);

        ordersRepository.save(orderEntity);
    }

    @EventHandler
    public void on(OrderApprovedEvent event) {
        OrderEntity orderEntity = ordersRepository.findByOrderId(event.getOrderId());
        if (orderEntity == null) {
            // TODO: Do something about it
            return;
        }
        orderEntity.setOrderStatus(event.getOrderStatus());
        ordersRepository.save(orderEntity);
    }

    @EventHandler
    public void on(OrderRejectedEvent event) {
        OrderEntity orderEntity = ordersRepository.findByOrderId(event.getOrderId());
        if(orderEntity == null) {
            // TODO: Do something about it
            return;
        }
        orderEntity.setOrderStatus(event.getOrderStatus());
        ordersRepository.save(orderEntity);
    }
}
