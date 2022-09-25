package com.appsdeveloperblog.estore.orderservice.saga;

import com.appsdeveloperblog.estore.core.commands.ProcessPaymentCommand;
import com.appsdeveloperblog.estore.core.commands.ReserveProductCommand;
import com.appsdeveloperblog.estore.core.events.PaymentProcessEvent;
import com.appsdeveloperblog.estore.core.events.ProductReservedEvent;
import com.appsdeveloperblog.estore.core.model.User;
import com.appsdeveloperblog.estore.core.query.FetchUserPaymentDetailsQuery;
import com.appsdeveloperblog.estore.orderservice.command.commands.ApproveOrderCommand;
import com.appsdeveloperblog.estore.orderservice.core.events.OrderApprovedEvent;
import com.appsdeveloperblog.estore.orderservice.core.events.OrderCreatedEvent;
import com.beust.ah.A;
import org.aspectj.weaver.ast.Or;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.spring.stereotype.Saga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Saga
public class OrderSaga {

    private static final Logger logger = LoggerFactory.getLogger(OrderSaga.class);
    @Autowired
    private transient CommandGateway commandGateway;
    @Autowired
    private transient QueryGateway queryGateway;

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    private void handle(OrderCreatedEvent orderCreatedEvent) {
        ReserveProductCommand reserveProductCommand = ReserveProductCommand.builder()
                .orderId(orderCreatedEvent.getOrderId())
                .productId(orderCreatedEvent.getProductId())
                .quantity(orderCreatedEvent.getQuantity())
                .userId(orderCreatedEvent.getUserId())
                .build();

        logger.info("OrderCreatedEvent handled for orderId: " + reserveProductCommand.getOrderId()
            + " and productId: " + reserveProductCommand.getProductId()
        );

        commandGateway.send(reserveProductCommand, new CommandCallback<ReserveProductCommand, Object>() {
            @Override
            public void onResult(@Nonnull CommandMessage<? extends ReserveProductCommand> commandMessage, @Nonnull CommandResultMessage<?> commandResultMessage) {
                if (commandResultMessage.isExceptional()) {
                    // Start a compensating transaction
                }
            }
        });
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservedEvent productReservedEvent) {
        // Process use payment
        logger.info("ProductReservedEvent is called for orderId: " + productReservedEvent.getOrderId()
            + " and productId: " + productReservedEvent.getProductId()
        );

        FetchUserPaymentDetailsQuery fetchUserPaymentDetailsQuery =
                new FetchUserPaymentDetailsQuery(productReservedEvent.getUserId());

        User userPaymentDetails = null;

        try {
            userPaymentDetails = queryGateway
                    .query(fetchUserPaymentDetailsQuery, ResponseTypes.instanceOf(User.class))
                    .join();
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            // Start compensating transaction
            return;
        }

        if (userPaymentDetails == null) {
            logger.error("No payment details are available for userId: " + productReservedEvent.getUserId());
            // Start compensation transaction
            return;
        }
        logger.info("Successfully fetched payment details for user " + userPaymentDetails.getFirstName());


        ProcessPaymentCommand processPaymentCommand = ProcessPaymentCommand.builder()
                .orderId(productReservedEvent.getOrderId())
                .paymentDetails(userPaymentDetails.getPaymentDetails())
                .paymentId(UUID.randomUUID().toString())
                .build();

        String result = null;
        try {
            result = commandGateway.sendAndWait(processPaymentCommand, 10, TimeUnit.SECONDS);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            // Start compensating transaction
            return;
        }

        if (result == null ){
            logger.info("The ProcessPaymentCommand resulted NULL. Initiating compensating transactions");
            // Start compensating transaction
        }
    }


    @SagaEventHandler(associationProperty = "orderId")
    public void handle(PaymentProcessEvent paymentProcessEvent) {
        // Send an ApproveOrderCommand
        ApproveOrderCommand approveOrderCommand =
                new ApproveOrderCommand(paymentProcessEvent.getOrderId());

        commandGateway.sendAndWait(approveOrderCommand);
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderApprovedEvent orderApprovedEvent) {
        logger.info("Order is approved. Order Saga is complete for orderId: " + orderApprovedEvent.getOrderId());
//        SagaLifecycle.end();
    }
}
