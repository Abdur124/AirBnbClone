package com.java.springboot.airbnbclone.controllers;

import com.java.springboot.airbnbclone.dtos.HotelDto;
import com.java.springboot.airbnbclone.dtos.HotelInfoDto;
import com.java.springboot.airbnbclone.dtos.HotelPriceDto;
import com.java.springboot.airbnbclone.dtos.HotelSearchRequestDto;
import com.java.springboot.airbnbclone.services.HotelService;
import com.java.springboot.airbnbclone.services.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hotels")
public class HotelBrowseController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private HotelService hotelService;

    @GetMapping("/search")
    public ResponseEntity<Page<HotelPriceDto>> searchHotels(@RequestBody HotelSearchRequestDto searchRequestDto) {
           Page<HotelPriceDto> page = inventoryService.searchHotels(searchRequestDto);
           return ResponseEntity.ok(page);
    }

    @GetMapping("/{hotelId}/info")
    public ResponseEntity<HotelInfoDto> getHotelInfo(@PathVariable Long hotelId) {
        return ResponseEntity.ok(hotelService.getHotelInfo(hotelId));
    }
}
