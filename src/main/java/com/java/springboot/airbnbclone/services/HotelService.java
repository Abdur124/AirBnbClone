package com.java.springboot.airbnbclone.services;

import com.java.springboot.airbnbclone.dtos.HotelDto;
import com.java.springboot.airbnbclone.dtos.HotelInfoDto;
import com.java.springboot.airbnbclone.entities.Hotel;
import org.springframework.http.ResponseEntity;

public interface HotelService {

    HotelDto findHotelById(Long id);

    HotelDto createHotel(HotelDto hotel);

    HotelDto updateHotelById(Long id, HotelDto hotel);

    void deleteHotelById(Long id);

    void activateHotel(Long hotelId);

    HotelInfoDto getHotelInfo(Long hotelId);
}
