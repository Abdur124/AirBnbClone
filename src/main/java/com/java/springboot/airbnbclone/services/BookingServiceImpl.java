package com.java.springboot.airbnbclone.services;

import com.java.springboot.airbnbclone.dtos.BookingDto;
import com.java.springboot.airbnbclone.dtos.BookingRequest;
import com.java.springboot.airbnbclone.dtos.GuestDto;
import com.java.springboot.airbnbclone.entities.*;
import com.java.springboot.airbnbclone.entities.enums.BookingStatus;
import com.java.springboot.airbnbclone.exceptions.ResourceNotFoundException;
import com.java.springboot.airbnbclone.repos.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public BookingDto initialiseBooking(BookingRequest bookingRequest) {

        Hotel hotel = hotelRepository.findById(bookingRequest.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id"+bookingRequest.getHotelId()));

        Room room = roomRepository.findById(bookingRequest.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id"+bookingRequest.getRoomId()));

        List<Inventory> inventories = inventoryRepository.findAndLockAvailableInventories(bookingRequest.getRoomId(),
                bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate(), bookingRequest.getRooms());

        Long daysCount = ChronoUnit.DAYS.between(bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate()) + 1;

        if(daysCount != inventories.size()) {
            throw new IllegalStateException("Rooms not Found for Dates Requested....!!");
        }

        // TODO: Derive basePrice from PricingStrategy Impl

        Booking booking = Booking.builder()
                .hotel(hotel)
                .room(room)
                .bookingStatus(BookingStatus.RESERVED)
                .amount(BigDecimal.TEN) // WILL BE UPDATED LATER
                .checkInDate(bookingRequest.getCheckInDate())
                .checkOutDate(bookingRequest.getCheckOutDate())
                .user(getCurrentUser())
                .roomsCount(inventories.size())
                .build();

        for(Inventory inventory : inventories) {
            inventory.setReservedCount(inventory.getReservedCount() + booking.getRoomsCount());
        }

        inventoryRepository.saveAll(inventories);

        Booking savedBooking = bookingRepository.save(booking);

        return modelMapper.map(savedBooking, BookingDto.class);
    }

    @Override
    public BookingDto addAllGuests(Long bookingId, List<GuestDto> guests) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id"+bookingId));

        if(!booking.getBookingStatus().equals(BookingStatus.RESERVED)) {
            throw new IllegalStateException("Booking is not reserved yet");
        }

        if(isBookingExpired(booking)) {
            throw new IllegalStateException("Booking is expired");
        }

        if(booking.getUser().equals(getCurrentUser())) {
            throw new IllegalStateException("Unauthorized booking");
        }

        for(GuestDto guestDto : guests) {
            Guest guest = modelMapper.map(guestDto, Guest.class);
            guest.setUser(getCurrentUser());
            guestRepository.save(guest);
            booking.getGuests().add(guest);
        }

        return modelMapper.map(bookingRepository.save(booking), BookingDto.class);
    }


    private boolean isBookingExpired(Booking booking) {

        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }

    public User getCurrentUser() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return userRepository.findUserByEmail(user.getEmail()).orElseThrow(() -> new RuntimeException("User not found"));
    }
}
