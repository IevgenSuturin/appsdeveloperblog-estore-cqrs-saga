package com.appsdeveloperblog.estore.productservice.command.rest;

import com.appsdeveloperblog.estore.productservice.command.CreateProductCommand;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductCommandController {
    private final CommandGateway commandGateway;

    public ProductCommandController(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @PostMapping
    public ResponseEntity createProduct(@Valid @RequestBody CreateProductRestModel model) {

        CreateProductCommand createProductCommand = CreateProductCommand.builder()
                .price(model.getPrice())
                .quantity(model.getQuantity())
                .title(model.getTitle())
                .productId(UUID.randomUUID().toString())
                .build();

        String returnValue = commandGateway.sendAndWait(createProductCommand);

        return ResponseEntity.ok(returnValue);
    }

}
