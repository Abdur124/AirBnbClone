package com.java.springboot.airbnbclone.repos;

import com.java.springboot.airbnbclone.entities.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

}
