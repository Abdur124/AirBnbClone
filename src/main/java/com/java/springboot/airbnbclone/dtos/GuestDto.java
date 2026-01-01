package com.java.springboot.airbnbclone.dtos;

import com.java.springboot.airbnbclone.entities.User;
import com.java.springboot.airbnbclone.entities.enums.Gender;
import jakarta.persistence.*;
import lombok.Data;

@Data
public class GuestDto {

    private Long id;
    private Long userId;
    private String name;
    private Gender gender;
    private Integer age;
}
