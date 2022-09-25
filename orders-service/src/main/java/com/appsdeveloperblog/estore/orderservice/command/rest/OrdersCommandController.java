package com.appsdeveloperblog.estore.orderservice.command.rest;

import com.appsdeveloperblog.estore.orderservice.command.commands.CreateOrderCommand;
import com.appsdeveloperblog.estore.orderservice.core.model.OrderStatus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrdersCommandController {

    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;

    public OrdersCommandController(CommandGateway commandGateway, QueryGateway queryGateway) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
    }

    @PostMapping
    public ResponseEntity createOrder(@Valid @RequestBody CreateOrderRestModel model) {

        CreateOrderCommand createOrderCommand = CreateOrderCommand.builder()
                .productId(model.getProductId())
                .quantity(model.getQuantity())
                .addressId(model.getAddressId())
                .orderId(UUID.randomUUID().toString())
                .userId("27b95829-4f3f-4ddf-8983-151ba010e35b")
                .orderStatus(OrderStatus.CREATED)
                .build();

        String returnValue = commandGateway.sendAndWait(createOrderCommand);

        return ResponseEntity.ok(returnValue);
    }
}
