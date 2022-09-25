package com.appdeveloperblog.estore.paymentsservice.events;

import com.appdeveloperblog.estore.paymentsservice.data.PaymentEntity;
import com.appdeveloperblog.estore.paymentsservice.data.PaymentsRepository;
import com.appsdeveloperblog.estore.core.events.PaymentProcessEvent;
import org.axonframework.eventhandling.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventsHandler {
    private final Logger logger = LoggerFactory.getLogger(PaymentEventsHandler.class);
    private final PaymentsRepository paymentsRepository;

    public PaymentEventsHandler(PaymentsRepository paymentsRepository) {
        this.paymentsRepository = paymentsRepository;
    }

    @EventHandler
    public void on(PaymentProcessEvent event) {
        logger.info("PaymentProcessEvent is called for orderId: " + event.getOrderId());

        PaymentEntity paymentEntity = new PaymentEntity();
        BeanUtils.copyProperties(event, paymentEntity);

        paymentsRepository.save(paymentEntity);
    }
}
