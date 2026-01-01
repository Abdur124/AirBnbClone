package com.java.springboot.airbnbclone.dtos;

import lombok.Data;

import java.time.LocalDate;

@Data
public class HotelSearchRequestDto {

    private String city;
    private LocalDate startDate;
    private LocalDate endDate;
    private int rooms;

    private int page=0;
    private int size=10;
}
