package com.java.springboot.airbnbclone.services;

import com.java.springboot.airbnbclone.dtos.HotelDto;
import com.java.springboot.airbnbclone.dtos.HotelInfoDto;
import com.java.springboot.airbnbclone.dtos.RoomDto;
import com.java.springboot.airbnbclone.entities.Hotel;
import com.java.springboot.airbnbclone.exceptions.ResourceNotFoundException;
import com.java.springboot.airbnbclone.repos.HotelRepository;
import com.java.springboot.airbnbclone.repos.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HotelServiceImpl implements HotelService{

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public HotelDto findHotelById(Long id) {
        log.info("Getting the hotel with id " + id);
        Hotel hotel = hotelRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id"+id));
        return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    public HotelDto createHotel(HotelDto hotelDto) {
        log.info("Creating Hotel with name {}", hotelDto.getName());
        Hotel hotel = modelMapper.map(hotelDto, Hotel.class);
        hotel.setActive(false);
        Hotel savedHotel = hotelRepository.save(hotel);
        log.info("Created a new Hotel with Id {}", savedHotel.getId());
        return modelMapper.map(savedHotel, HotelDto.class);
    }

    @Override
    public HotelDto updateHotelById(Long id, HotelDto hotelDto) {
        log.info("Updating Hotel with id {}", id);
        Hotel hotel = hotelRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id"+id));
        modelMapper.map(hotelDto, hotel);
        hotel.setId(id);
        Hotel savedHotel = hotelRepository.save(hotel);
        return modelMapper.map(savedHotel, HotelDto.class);
    }

    @Override
    @Transactional
    public void deleteHotelById(Long id) {
        Hotel hotel = hotelRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: "+id));
        hotel.getRooms().forEach(room -> {
            inventoryService.deleteAllInventories(room);
            roomRepository.delete(room);
        });
        hotelRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void activateHotel(Long hotelId) {
        log.info("Activating Hotel with id {}", hotelId);
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id"+hotelId));
        hotel.setId(hotelId);
        hotel.setActive(true);
        hotelRepository.save(hotel);

        hotel.getRooms().forEach(room -> {
            inventoryService.initializeRoomForAYear(room);
        });
    }

    @Override
    public HotelInfoDto getHotelInfo(Long hotelId) {
        log.info("Getting HotelInfo with id {}", hotelId);
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id"+hotelId));
        List<RoomDto> rooms = hotel.getRooms().stream().map(room -> modelMapper.map(room, RoomDto.class)).collect(Collectors.toList());
        HotelDto hotelDto = modelMapper.map(hotel, HotelDto.class);
        return new HotelInfoDto(hotelDto, rooms);
    }


}
