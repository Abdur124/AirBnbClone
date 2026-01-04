package com.java.springboot.airbnbclone.repos;

import com.java.springboot.airbnbclone.entities.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByPaymentSessionId(String sessionId);
}
