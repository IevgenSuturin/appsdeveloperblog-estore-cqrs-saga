package com.appsdeveloperblog.estore.productservice.command;

import com.appsdeveloperblog.estore.core.commands.CancelProductReservationCommand;
import com.appsdeveloperblog.estore.core.commands.ReserveProductCommand;
import com.appsdeveloperblog.estore.core.events.ProductReservationCanceledEvent;
import com.appsdeveloperblog.estore.core.events.ProductReservedEvent;
import com.appsdeveloperblog.estore.productservice.core.events.ProductCreatedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;

@Aggregate(snapshotTriggerDefinition = "productSnapshotTriggerDefinition")
public class ProductAggregate {

    @AggregateIdentifier
    private String productId;
    private String title;
    private BigDecimal price;
    private Integer quantity;

    public ProductAggregate() {
    }

    @CommandHandler
    public ProductAggregate(CreateProductCommand createProductCommand) {
        //Validate Create Product Command
        if (createProductCommand.getPrice().compareTo(BigDecimal.ZERO) <= 0 ) {
            throw new IllegalArgumentException("Price can not be less or equal to zero");
        }
        if (createProductCommand.getTitle() == null || createProductCommand.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title can not be empty");
        }

        ProductCreatedEvent productCreatedEvent = new ProductCreatedEvent();
        BeanUtils.copyProperties(createProductCommand, productCreatedEvent);
        AggregateLifecycle.apply(productCreatedEvent);
    }

    @EventSourcingHandler
    public void on(ProductCreatedEvent productCreatedEvent) {
        this.productId = productCreatedEvent.getProductId();
        this.title = productCreatedEvent.getTitle();
        this.price = productCreatedEvent.getPrice();
        this.quantity = productCreatedEvent.getQuantity();
    }

    @CommandHandler
    public void handle(ReserveProductCommand reserveProductCommand) {

        if (quantity < reserveProductCommand.getQuantity()) {
            throw new IllegalArgumentException("Insufficient number of items in stock");
        }

        ProductReservedEvent productReservedEvent = ProductReservedEvent.builder()
                .orderId(reserveProductCommand.getOrderId())
                .productId(reserveProductCommand.getProductId())
                .quantity(reserveProductCommand.getQuantity())
                .userId(reserveProductCommand.getUserId())
                .build();
        AggregateLifecycle.apply(productReservedEvent);
    }

    @EventSourcingHandler
    public void on(ProductReservedEvent productReservedEvent) {
        this.quantity -= productReservedEvent.getQuantity();
    }

    @CommandHandler
    public void handle(CancelProductReservationCommand cancelProductReservationCommand) {
        ProductReservationCanceledEvent productReservationCanceledEvent = ProductReservationCanceledEvent.builder()
                .productId(cancelProductReservationCommand.getProductId())
                .quantity(cancelProductReservationCommand.getQuantity())
                .orderId(cancelProductReservationCommand.getOrderId())
                .userId(cancelProductReservationCommand.getUserId())
                .reason(cancelProductReservationCommand.getReason())
                .build();

        AggregateLifecycle.apply(productReservationCanceledEvent);
    }

    @EventSourcingHandler
    public void on(ProductReservationCanceledEvent event) {
        this.quantity += event.getQuantity();
    }
}
