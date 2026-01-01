package com.java.springboot.airbnbclone.repos;

import com.java.springboot.airbnbclone.entities.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {

}
