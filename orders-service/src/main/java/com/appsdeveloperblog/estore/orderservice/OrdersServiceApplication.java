package com.appsdeveloperblog.estore.orderservice;

import com.appsdeveloperblog.estore.core.config.AxonConfig;
import com.appsdeveloperblog.estore.orderservice.core.errorhandling.OrdersServiceEventsErrorHandler;
import org.axonframework.config.EventProcessingConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

@EnableDiscoveryClient
@SpringBootApplication
@Import({AxonConfig.class})
public class OrdersServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrdersServiceApplication.class, args);
    }

    @Autowired
    public void configure(EventProcessingConfigurer config) {
        config.registerListenerInvocationErrorHandler(
                "order-group",
                configuration -> new OrdersServiceEventsErrorHandler()
        );
    }
}
