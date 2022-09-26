package com.appsdeveloperblog.estore.orderservice.command;

import com.appsdeveloperblog.estore.orderservice.command.commands.ApproveOrderCommand;
import com.appsdeveloperblog.estore.orderservice.command.commands.CreateOrderCommand;
import com.appsdeveloperblog.estore.orderservice.command.commands.RejectOrderCommand;
import com.appsdeveloperblog.estore.orderservice.core.events.OrderApprovedEvent;
import com.appsdeveloperblog.estore.orderservice.core.events.OrderCreatedEvent;
import com.appsdeveloperblog.estore.orderservice.core.events.OrderRejectedEvent;
import com.appsdeveloperblog.estore.orderservice.core.model.OrderStatus;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;

@Aggregate
public class OrderAggregate {
    @AggregateIdentifier
    private String orderId;
    private String productId;
    private String userId;
    private int quantity;
    private String addressId;
    private OrderStatus orderStatus;

    public OrderAggregate(){
    }

    @CommandHandler
    public OrderAggregate(CreateOrderCommand createOrderCommand) {
        //Validate Create Product Command
        if (createOrderCommand.getQuantity() <= 0 ) {
            throw new IllegalArgumentException("Quantity can not be less or equal to zero");
        }
        if (createOrderCommand.getProductId() == null || createOrderCommand.getProductId().isBlank()) {
            throw new IllegalArgumentException("Product Id can not be empty");
        }
        if (createOrderCommand.getAddressId() == null || createOrderCommand.getAddressId().isBlank()) {
            throw new IllegalArgumentException("Address Id can not be empty");
        }

        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent();
        BeanUtils.copyProperties(createOrderCommand, orderCreatedEvent);
        AggregateLifecycle.apply(orderCreatedEvent);
    }

    @EventSourcingHandler
    public void on(OrderCreatedEvent orderCreatedEvent) {
        this.orderId = orderCreatedEvent.getOrderId();
        this.productId = orderCreatedEvent.getProductId();
        this.userId = orderCreatedEvent.getUserId();
        this.quantity = orderCreatedEvent.getQuantity();
        this.addressId = orderCreatedEvent.getAddressId();
        this.orderStatus = orderCreatedEvent.getOrderStatus();
    }

    @CommandHandler
    public void handle(ApproveOrderCommand approveOrderCommand) {
        // Create and publish OrderApproveEvent
        OrderApprovedEvent orderApprovedEvent =
                new OrderApprovedEvent(approveOrderCommand.getOrderId());
        AggregateLifecycle.apply(orderApprovedEvent);
    }

    @EventSourcingHandler
    protected void on(OrderApprovedEvent orderApprovedEvent) {
        this.orderStatus = orderApprovedEvent.getOrderStatus();
    }

    @CommandHandler
    public void handle(RejectOrderCommand rejectOrderCommand) {
        OrderRejectedEvent orderRejectedEvent = new OrderRejectedEvent(
                rejectOrderCommand.getOrderId(),
                rejectOrderCommand.getReason()
        );
        AggregateLifecycle.apply(orderRejectedEvent);
    }

    @EventSourcingHandler
    public void on(OrderRejectedEvent orderRejectedEvent) {
        this.orderStatus = orderRejectedEvent.getOrderStatus();
    }
}
