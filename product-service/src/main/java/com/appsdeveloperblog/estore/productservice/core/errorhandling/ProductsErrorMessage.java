package com.appsdeveloperblog.estore.productservice.core.errorhandling;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class ProductsErrorMessage {

    private final Date timestamp;
    private final String message;
}
