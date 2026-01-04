package com.java.springboot.airbnbclone.services;

import com.java.springboot.airbnbclone.entities.Booking;
import com.java.springboot.airbnbclone.entities.User;
import com.java.springboot.airbnbclone.repos.BookingRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class CheckoutServiceImpl implements CheckoutService {

    @Autowired
    private BookingRepository bookingRepository;

    @Override
    public String getCheckoutSession(Booking booking, String successUrl, String failureUrl) {

        log.info("Creating Checkout session for Booking with Id: {}", booking.getId());

        User user = booking.getUser();

        try {

            CustomerCreateParams customerCreateParams = CustomerCreateParams.builder()
                    .setName(user.getName())
                    .setEmail(user.getEmail())
                    .build();

            Customer customer = Customer.create(customerCreateParams);

            SessionCreateParams params =
                    SessionCreateParams.builder()
                            .setMode(SessionCreateParams.Mode.PAYMENT)
                            .setBillingAddressCollection(SessionCreateParams.BillingAddressCollection.REQUIRED)
                            .setCustomer(customer.getId())
                            .setSuccessUrl(successUrl)
                            .addLineItem(
                                    SessionCreateParams.LineItem.builder()
                                            .setQuantity(1L)
                                            // Provide the exact Price ID (for example, price_1234) of the product you want to sell
                                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("INR")
                                                    .setUnitAmount(booking.getAmount().multiply(BigDecimal.valueOf(100)).longValueExact())
                                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                            .setName(booking.getHotel().getName() + ":" + booking.getRoom().getType())
                                                            .build())
                                                    .build())
                                            .build())
                    .build();

            Session session = Session.create(params);
            booking.setPaymentSessionId(session.getId());

            bookingRepository.save(booking);

            log.info("Session created successfully for booking with ID: {}", booking.getId());
            return session.getUrl();
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }
}
