package com.java.springboot.airbnbclone.services;

import com.java.springboot.airbnbclone.dtos.BookingDto;
import com.java.springboot.airbnbclone.dtos.BookingRequest;
import com.java.springboot.airbnbclone.dtos.GuestDto;
import com.stripe.model.Event;

import java.util.List;

public interface BookingService {

    BookingDto initialiseBooking(BookingRequest bookingRequest);

    BookingDto addAllGuests(Long bookingId, List<GuestDto> guests);

    String initiatePayments(Long bookingId);

    void capturePayment(Event event);
}
