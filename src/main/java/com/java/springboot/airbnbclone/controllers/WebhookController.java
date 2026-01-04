package com.java.springboot.airbnbclone.controllers;

import com.java.springboot.airbnbclone.services.BookingService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WebhookController {

    @Autowired
    private final BookingService bookingService;

    @Value("${stripe.webhook.secretKey}")
    private String endpointSecret;

    @PostMapping("/payment")
    public ResponseEntity<Void> capturePayments(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {

        try {
             Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
             bookingService.capturePayment(event);
             return ResponseEntity.noContent().build();
        } catch (SignatureVerificationException e) {
            throw new RuntimeException(e);
        }
    }
}
