package com.appsdeveloperblog.estore.orderservice.query;

import com.appsdeveloperblog.estore.orderservice.core.data.OrderEntity;
import com.appsdeveloperblog.estore.orderservice.core.data.OrdersRepository;
import com.appsdeveloperblog.estore.orderservice.core.model.OrderSummary;
import com.appsdeveloperblog.estore.orderservice.query.rest.OrdersRestModel;
import org.aspectj.weaver.ast.Or;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrdersQueryHandler {

    private final OrdersRepository ordersRepository;

    public OrdersQueryHandler(OrdersRepository ordersRepository) {
        this.ordersRepository = ordersRepository;
    }

    @QueryHandler
    public OrderSummary findOrder(FindOrderQuery findOrderQuery){
        OrderEntity orderEntity = ordersRepository.findByOrderId(findOrderQuery.getOrderId());
        return new OrderSummary(orderEntity.getOrderId(),
                orderEntity.getOrderStatus(), "");
    }

    @QueryHandler
    public List<OrdersRestModel> findAllOrders(FindOrdersQuery findOrdersQuery) {
        List<OrdersRestModel> ordersList = new ArrayList<>();
        List<OrderEntity> storedOrders = ordersRepository.findAll();

        for (OrderEntity storedOrder: storedOrders) {
            OrdersRestModel ordersRestModel = new OrdersRestModel();
            BeanUtils.copyProperties(storedOrder, ordersRestModel);
            ordersList.add(ordersRestModel);
        }

        return ordersList;
    }
}
