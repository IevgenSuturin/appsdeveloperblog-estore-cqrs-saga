package com.appsdeveloperblog.estore.orderservice.core.data;

import com.appsdeveloperblog.estore.orderservice.core.model.OrderStatus;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "orders")
public class OrderEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(unique = true)
    public String orderId;

    private String userId;
    private String productId;
    private int quantity;
    private String addressId;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;
}
