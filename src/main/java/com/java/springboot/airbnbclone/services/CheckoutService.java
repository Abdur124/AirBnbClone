package com.java.springboot.airbnbclone.services;

import com.java.springboot.airbnbclone.entities.Booking;

public interface CheckoutService {

    String getCheckoutSession(Booking booking, String successUrl, String failureUrl);
}
