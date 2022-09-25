package com.appdeveloperblog.estore.paymentsservice.command;

import com.appsdeveloperblog.estore.core.commands.ProcessPaymentCommand;
import com.appsdeveloperblog.estore.core.events.PaymentProcessEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
public class PaymentAggregate {

    @AggregateIdentifier
    private String paymentId;
    private String orderId;

    public PaymentAggregate() {
    }

    @CommandHandler
    public PaymentAggregate(ProcessPaymentCommand processPaymentCommand) {
        if (processPaymentCommand.getPaymentDetails() == null) {
            throw new IllegalArgumentException("Missing Payment Details");
        }

        if (processPaymentCommand.getPaymentId() == null) {
            throw new IllegalArgumentException("Missing Payment Identifier");
        }

        if (processPaymentCommand.getOrderId() == null) {
            throw new IllegalArgumentException("Missing Order Identifier");
        }


        PaymentProcessEvent paymentProcessEvent =PaymentProcessEvent.builder()
                .paymentId(processPaymentCommand.getPaymentId())
                .orderId(processPaymentCommand.getOrderId())
                .build();

        AggregateLifecycle.apply(paymentProcessEvent);
    }

    @EventSourcingHandler
    public void on(PaymentProcessEvent paymentProcessEvent) {
        this.paymentId = paymentProcessEvent.getPaymentId();
        this.orderId = paymentProcessEvent.getOrderId();
    }
}
