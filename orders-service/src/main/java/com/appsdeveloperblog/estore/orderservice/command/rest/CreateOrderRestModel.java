package com.appsdeveloperblog.estore.orderservice.command.rest;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
public class CreateOrderRestModel {
    @NotBlank(message = "Product Id can not be empty")
    private String productId;
    @Min(value = 1, message = "Quantity can not be lower than 1")
    private int quantity;
    @NotBlank(message = "Address Id can not be empty")
    private String addressId;
}
