package com.java.springboot.airbnbclone.exceptions;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String s) {
        super(s);
    }

    public UserAlreadyExistsException() {

    }
}
