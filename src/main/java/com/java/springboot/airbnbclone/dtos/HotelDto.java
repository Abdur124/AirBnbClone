package com.java.springboot.airbnbclone.dtos;

import com.java.springboot.airbnbclone.entities.HotelContactInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class HotelDto {

    private Long id;
    private String name;
    private String city;
    private List<String> photos;
    private List<String> amenities;
    private HotelContactInfo hotelContactInfo;
    private Boolean active;
}
