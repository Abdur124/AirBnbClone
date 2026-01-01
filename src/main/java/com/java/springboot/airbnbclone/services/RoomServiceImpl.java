package com.java.springboot.airbnbclone.services;

import com.java.springboot.airbnbclone.dtos.RoomDto;
import com.java.springboot.airbnbclone.entities.Hotel;
import com.java.springboot.airbnbclone.entities.Room;
import com.java.springboot.airbnbclone.exceptions.ResourceNotFoundException;
import com.java.springboot.airbnbclone.repos.HotelRepository;
import com.java.springboot.airbnbclone.repos.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RoomServiceImpl implements RoomService{

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional
    public RoomDto createNewRoom(Long hotelId, RoomDto roomDto) {
        log.info("Creating Room for Hotel with id {}", hotelId);
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id"+hotelId));
        Room room = modelMapper.map(roomDto, Room.class);
        room.setHotel(hotel);
        Room savedRoom = roomRepository.save(room);

        if(hotel.getActive()) {
            log.info("Room with id {} has been activated", savedRoom.getId());
            inventoryService.initializeRoomForAYear(savedRoom);
        }
        return modelMapper.map(savedRoom, RoomDto.class);
    }

    @Override
    public List<RoomDto> getAllRoomsInHotel(Long hotelId) {
        log.info("Getting all Rooms In Hotel with id {}", hotelId);
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(() ->
                new ResourceNotFoundException("Hotel not found with id"+hotelId));

        return hotel.getRooms().stream().map(r -> modelMapper.map(r, RoomDto.class)).collect(Collectors.toList());
    }

    @Override
    public RoomDto getRoomById(Long id) {
        log.info("Getting Room with id {}", id);
        Room room = roomRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Room not found with id"+id));
        return modelMapper.map(room, RoomDto.class);
    }

    @Override
    @Transactional
    public void deleteRoomById(Long id) {
        log.info("Deleting Room with id {}", id);
        Room room = roomRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Room not found with id"+id));
        inventoryService.deleteAllInventories(room);
        roomRepository.deleteById(id);
    }
}
