package com.appsdeveloperblog.estore.productservice;

import com.appsdeveloperblog.estore.core.config.AxonConfig;
import com.appsdeveloperblog.estore.productservice.command.interceprots.CreateProductCommandInterceptor;
import com.appsdeveloperblog.estore.productservice.core.errorhandling.ProductsServiceEventsErrorHandler;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.config.EventProcessingConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

@EnableDiscoveryClient
@SpringBootApplication
@Import({AxonConfig.class})
public class ProductServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }

    //Register command interceptor
    @Autowired
    public void registerCreateProductCommandInterceptor(ApplicationContext applicationContext,
                                                        CommandBus commandBus) {
        commandBus.registerDispatchInterceptor(
                applicationContext.getBean(CreateProductCommandInterceptor.class)
        );
    }

    @Autowired
    public void configure(EventProcessingConfigurer config) {
        config.registerListenerInvocationErrorHandler(
                "product-group",
                configuration -> new ProductsServiceEventsErrorHandler()
        );

//        config.registerListenerInvocationErrorHandler(
//                "product-group",
//                configuration -> PropagatingErrorHandler.instance()
//        );
    }
}