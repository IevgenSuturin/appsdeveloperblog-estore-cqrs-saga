package com.appsdeveloperblog.estore.orderservice.core.errorhandling;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class OrdersErrorMessage {

    private final Date timestamp;
    private final String message;
}
