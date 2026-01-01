package com.java.springboot.airbnbclone.dtos;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingRequest {

    private Long hotelId;
    private Long roomId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int rooms;
}
