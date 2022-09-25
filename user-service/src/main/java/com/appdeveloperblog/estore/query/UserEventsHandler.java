package com.appdeveloperblog.estore.query;

import com.appsdeveloperblog.estore.core.model.PaymentDetails;
import com.appsdeveloperblog.estore.core.model.User;
import com.appsdeveloperblog.estore.core.query.FetchUserPaymentDetailsQuery;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class UserEventsHandler {

    @QueryHandler
    public User findUserPaymentDetails(FetchUserPaymentDetailsQuery query) {
        PaymentDetails paymentDetails = PaymentDetails.builder()
                .cardNumber("123Card")
                .cvv("1234")
                .name("YEVHEN SUTURIN")
                .validUntilMonth(12)
                .validUntilYear(2031)
                .build();

        User user = User.builder()
                .firstName("Yevhen")
                .lastName("Suturin")
                .userId(query.getUserId())
                .paymentDetails(paymentDetails)
                .build();

        return user;
    }
}
