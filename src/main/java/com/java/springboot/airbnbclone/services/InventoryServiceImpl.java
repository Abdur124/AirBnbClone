package com.java.springboot.airbnbclone.services;

import com.java.springboot.airbnbclone.dtos.HotelDto;
import com.java.springboot.airbnbclone.dtos.HotelPriceDto;
import com.java.springboot.airbnbclone.dtos.HotelSearchRequestDto;
import com.java.springboot.airbnbclone.entities.Hotel;
import com.java.springboot.airbnbclone.entities.Inventory;
import com.java.springboot.airbnbclone.entities.Room;
import com.java.springboot.airbnbclone.repos.HotelMinPriceRepository;
import com.java.springboot.airbnbclone.repos.InventoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
public class InventoryServiceImpl implements InventoryService{

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private HotelMinPriceRepository hotelMinPriceRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public void initializeRoomForAYear(Room room) {

        log.info("Initializing room for a year for room with id " + room.getId());
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusYears(1);

        for(; !today.isAfter(endDate); today=today.plusDays(1)) {
                Inventory inventory = Inventory.builder()
                        .hotel(room.getHotel())
                        .room(room)
                        .bookedCount(0)
                        .reservedCount(0)
                        .city(room.getHotel().getCity())
                        .price(room.getBasePrice())
                        .surgeFactor(BigDecimal.ONE)
                        .date(today)
                        .totalCount(room.getTotalCount())
                        .closed(false)
                        .build();
                inventoryRepository.save(inventory);
        }
    }

    @Override
    public void deleteAllInventories(Room room) {
            LocalDate today = LocalDate.now();
            inventoryRepository.deleteByRoom(room);
    }

    @Override
    public Page<HotelPriceDto> searchHotels(HotelSearchRequestDto hotelSearchRequest) {
        Pageable pageable = PageRequest.of(hotelSearchRequest.getPage(), hotelSearchRequest.getSize());

        Long dateCount = ChronoUnit.DAYS.between(hotelSearchRequest.getStartDate(), hotelSearchRequest.getEndDate()) + 1;
        Page<HotelPriceDto> hotelPage = hotelMinPriceRepository.findHotelsWithAvailableInventory(hotelSearchRequest.getCity(), hotelSearchRequest.getStartDate(),
                hotelSearchRequest.getEndDate(), pageable);
        return hotelPage;
    }


}
