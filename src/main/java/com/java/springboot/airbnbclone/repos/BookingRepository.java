package com.java.springboot.airbnbclone.repos;

import com.java.springboot.airbnbclone.entities.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
}
