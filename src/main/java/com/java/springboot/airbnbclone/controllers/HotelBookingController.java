package com.java.springboot.airbnbclone.controllers;

import com.java.springboot.airbnbclone.dtos.BookingDto;
import com.java.springboot.airbnbclone.dtos.BookingRequest;
import com.java.springboot.airbnbclone.dtos.GuestDto;
import com.java.springboot.airbnbclone.services.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bookings")
public class HotelBookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingDto> initialiseBooking(@RequestBody BookingRequest bookingRequest) {
        return ResponseEntity.ok(bookingService.initialiseBooking(bookingRequest));
    }

    @PostMapping("/{bookingId}/addGuests")
    public ResponseEntity<BookingDto> addGuests(@PathVariable Long bookingId, @RequestBody List<GuestDto> guests) {
        return ResponseEntity.ok(bookingService.addAllGuests(bookingId, guests));
    }

    @PostMapping("/{bookingId}/payments")
    public ResponseEntity<Map<String, String>> addGuests(@PathVariable Long bookingId) {
        String sessionUrl = bookingService.initiatePayments(bookingId);
        return ResponseEntity.ok(Map.of("sessionUrl", sessionUrl));
    }
}
