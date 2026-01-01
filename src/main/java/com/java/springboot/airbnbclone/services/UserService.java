package com.java.springboot.airbnbclone.services;

import com.java.springboot.airbnbclone.entities.User;

public interface UserService {

    User findUserByEmail(String email);

    User saveUserToDB(User newUser);
}
