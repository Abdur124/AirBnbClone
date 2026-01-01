package com.java.springboot.airbnbclone.dtos;

import com.java.springboot.airbnbclone.entities.Guest;
import com.java.springboot.airbnbclone.entities.Hotel;
import com.java.springboot.airbnbclone.entities.Room;
import com.java.springboot.airbnbclone.entities.User;
import com.java.springboot.airbnbclone.entities.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class BookingDto {

    private Long id;
    private Integer roomsCount;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BookingStatus bookingStatus;
    private Set<GuestDto> guests;
}
