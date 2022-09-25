package com.appsdeveloperblog.estore.core.events;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentProcessEvent {
    private final String orderId;
    private final String paymentId;
}
