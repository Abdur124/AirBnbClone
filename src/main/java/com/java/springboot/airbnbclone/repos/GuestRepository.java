package com.java.springboot.airbnbclone.repos;

import com.java.springboot.airbnbclone.entities.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestRepository extends JpaRepository<Guest, Long> {

}
