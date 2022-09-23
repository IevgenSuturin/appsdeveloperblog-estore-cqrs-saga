package com.appsdeveloperblog.estore.orderservice.saga;

import com.appsdeveloperblog.estore.core.commands.ReserveProductCommand;
import com.appsdeveloperblog.estore.orderservice.core.events.OrderCreatedEvent;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;

@Saga
public class OrderSaga {

    @Autowired
    private transient CommandGateway commandGateway;

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    private void handle(OrderCreatedEvent orderCreatedEvent) {
        ReserveProductCommand reserveProductCommand = ReserveProductCommand.builder()
                .orderId(orderCreatedEvent.getOrderId())
                .productId(orderCreatedEvent.getProductId())
                .quantity(orderCreatedEvent.getQuantity())
                .userId(orderCreatedEvent.getUserId())
                .build();

        commandGateway.send(reserveProductCommand, new CommandCallback<ReserveProductCommand, Object>() {
            @Override
            public void onResult(@Nonnull CommandMessage<? extends ReserveProductCommand> commandMessage, @Nonnull CommandResultMessage<?> commandResultMessage) {
                if (commandResultMessage.isExceptional()) {
                    // Start a compensating transaction
                }
            }
        });
    }

}
