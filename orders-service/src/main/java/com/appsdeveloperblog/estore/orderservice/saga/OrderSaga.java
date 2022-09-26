package com.appsdeveloperblog.estore.orderservice.saga;

import com.appsdeveloperblog.estore.core.commands.CancelProductReservationCommand;
import com.appsdeveloperblog.estore.core.commands.ProcessPaymentCommand;
import com.appsdeveloperblog.estore.core.commands.ReserveProductCommand;
import com.appsdeveloperblog.estore.core.events.PaymentProcessedEvent;
import com.appsdeveloperblog.estore.core.events.ProductReservationCanceledEvent;
import com.appsdeveloperblog.estore.core.events.ProductReservedEvent;
import com.appsdeveloperblog.estore.core.model.User;
import com.appsdeveloperblog.estore.core.query.FetchUserPaymentDetailsQuery;
import com.appsdeveloperblog.estore.orderservice.command.commands.ApproveOrderCommand;
import com.appsdeveloperblog.estore.orderservice.command.commands.RejectOrderCommand;
import com.appsdeveloperblog.estore.orderservice.core.events.OrderApprovedEvent;
import com.appsdeveloperblog.estore.orderservice.core.events.OrderCreatedEvent;
import com.appsdeveloperblog.estore.orderservice.core.events.OrderRejectedEvent;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.spring.stereotype.Saga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Saga
public class OrderSaga {

    private static final Logger logger = LoggerFactory.getLogger(OrderSaga.class);

    @Autowired
    private transient CommandGateway commandGateway;

    @Autowired
    private transient QueryGateway queryGateway;

    @Autowired
    private transient DeadlineManager deadlineManager;

    private static final String PAYMENT_PROCESSING_TIMEOUT_DEADLINE = "payment-processing-deadline";
    private String scheduleId;

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
            cancelProductReservation(productReservedEvent, ex.getMessage());
            return;
        }

        if (userPaymentDetails == null) {
            logger.error("No payment details are available for userId: " + productReservedEvent.getUserId());
            cancelProductReservation(productReservedEvent, "Could not fetch user payment details");
            return;
        }
        logger.info("Successfully fetched payment details for user " + userPaymentDetails.getFirstName());

         scheduleId = deadlineManager.schedule(Duration.of(120, ChronoUnit.SECONDS),
                PAYMENT_PROCESSING_TIMEOUT_DEADLINE,
                productReservedEvent);

        ProcessPaymentCommand processPaymentCommand = ProcessPaymentCommand.builder()
                .orderId(productReservedEvent.getOrderId())
                .paymentDetails(userPaymentDetails.getPaymentDetails())
                .paymentId(UUID.randomUUID().toString())
                .build();

        String result = null;
        try {
            result = commandGateway.sendAndWait(processPaymentCommand);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            cancelProductReservation(productReservedEvent, ex.getMessage());
            return;
        }

        if (result == null ){
            logger.info("The ProcessPaymentCommand resulted NULL. Initiating compensating transactions");
            cancelProductReservation(productReservedEvent, "Could not process user payment with provided payment details");
            return;
        }
    }

    private void cancelProductReservation(ProductReservedEvent productReservedEvent, String reason) {

        canselDeadLine();
        
        CancelProductReservationCommand publishProductReservationCommand =
                CancelProductReservationCommand.builder()
                        .productId(productReservedEvent.getProductId())
                        .quantity(productReservedEvent.getQuantity())
                        .orderId(productReservedEvent.getOrderId())
                        .reason(reason)
                        .build();

        commandGateway.send(publishProductReservationCommand);
    }


    @SagaEventHandler(associationProperty = "orderId")
    public void handle(PaymentProcessedEvent paymentProcessedEvent) {
        canselDeadLine();

        // Send an ApproveOrderCommand
        ApproveOrderCommand approveOrderCommand =
                new ApproveOrderCommand(paymentProcessedEvent.getOrderId());

        commandGateway.sendAndWait(approveOrderCommand);
    }

    private void canselDeadLine() {
        if (scheduleId != null) {
//            deadlineManager.cancelAll(PAYMENT_PROCESSING_TIMEOUT_DEADLINE);
            deadlineManager.cancelSchedule(PAYMENT_PROCESSING_TIMEOUT_DEADLINE, scheduleId);
            scheduleId = null;
        }
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderApprovedEvent orderApprovedEvent) {
        logger.info("Order is approved. Order Saga is complete for orderId: " + orderApprovedEvent.getOrderId());
//        SagaLifecycle.end();
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservationCanceledEvent productReservationCanceledEvent) {
        // Create and send RejectOrderCommand
        RejectOrderCommand rejectOrderCommand = new RejectOrderCommand(
                productReservationCanceledEvent.getOrderId(),
                productReservationCanceledEvent.getReason()
        );

        commandGateway.send(rejectOrderCommand);
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderRejectedEvent orderRejectedEvent) {
        logger.info("Successfully rejected order with orderId: " + orderRejectedEvent.getOrderId());
    }

    @DeadlineHandler(deadlineName = PAYMENT_PROCESSING_TIMEOUT_DEADLINE)
    public void handlePaymentDeadLine(ProductReservedEvent productReservedEvent) {
        logger.info("Payment processing deadline took place. Sending a compensating command to cansel the product reservation.");
        cancelProductReservation(productReservedEvent, "Payment timeout");
    }
}
