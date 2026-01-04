package com.java.springboot.airbnbclone.services;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.java.springboot.airbnbclone.dtos.BookingDto;
import com.java.springboot.airbnbclone.dtos.BookingRequest;
import com.java.springboot.airbnbclone.dtos.GuestDto;
import com.java.springboot.airbnbclone.entities.*;
import com.java.springboot.airbnbclone.entities.enums.BookingStatus;
import com.java.springboot.airbnbclone.exceptions.ResourceNotFoundException;
import com.java.springboot.airbnbclone.repos.*;
import com.java.springboot.airbnbclone.strategy.PricingService;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private CheckoutService checkoutService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PricingService pricingService;

    @Value("${app.baseUrl}")
    private String baseUrl;

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

        BigDecimal priceForOneRoom = pricingService.calculateTotalPrice(inventories);
        BigDecimal totalPrice = priceForOneRoom.multiply(BigDecimal.valueOf(bookingRequest.getRooms()));

        Booking booking = Booking.builder()
                .hotel(hotel)
                .room(room)
                .bookingStatus(BookingStatus.RESERVED)
                .amount(totalPrice) // WILL BE UPDATED LATER
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

    @Override
    public String initiatePayments(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new ResourceNotFoundException("Booking not found with id: "+bookingId)
        );
        User user = getCurrentUser();
     /*   if (!user.equals(booking.getUser())) {
            throw new RuntimeException("Booking does not belong to this user with id: "+user.getId());
        }*/
        if (isBookingExpired(booking)) {
            throw new IllegalStateException("Booking has already expired");
        }

        String sessionUrl = checkoutService.getCheckoutSession(booking,
                baseUrl+"/payments/" +bookingId +"/success",
                baseUrl+"/payments/" +bookingId +"/failure");

        booking.setBookingStatus(BookingStatus.PAYMENT_PENDING);
        bookingRepository.save(booking);

        return sessionUrl;
    }

    @Override
    @Transactional
    public void capturePayment(Event event) {

        if ("checkout.session.completed".equals(event.getType())) {

            String rawJson = event.getData().getObject().toJson();

            JSONObject jsonObject = new JSONObject(rawJson);
            String sessionId = jsonObject.getString("id");

            Booking booking = bookingRepository.findByPaymentSessionId(sessionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking not found for sessionId: "+sessionId));

            booking.setBookingStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(), booking.getCheckInDate(),
                    booking.getCheckOutDate(), booking.getRoomsCount());

            inventoryRepository.confirmBooking(booking.getRoom().getId(), booking.getCheckInDate(),
                    booking.getCheckOutDate(), booking.getRoomsCount());

            log.info("Successfully confirmed the booking for Booking ID: {}", booking.getId());
        } else {
            log.warn("Unhandled event type: {}", event.getType());
        }
    }


    private boolean isBookingExpired(Booking booking) {

        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }

    public User getCurrentUser() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return userRepository.findUserByEmail(user.getEmail()).orElseThrow(() -> new RuntimeException("User not found"));
    }
}
