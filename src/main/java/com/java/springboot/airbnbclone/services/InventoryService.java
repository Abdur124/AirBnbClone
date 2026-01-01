package com.java.springboot.airbnbclone.services;

import com.java.springboot.airbnbclone.dtos.HotelDto;
import com.java.springboot.airbnbclone.dtos.HotelPriceDto;
import com.java.springboot.airbnbclone.dtos.HotelSearchRequestDto;
import com.java.springboot.airbnbclone.entities.Room;
import org.springframework.data.domain.Page;

public interface InventoryService {

    void initializeRoomForAYear(Room room);

    void deleteAllInventories(Room room);

    Page<HotelPriceDto> searchHotels(HotelSearchRequestDto hotelSearchRequest);
}
