package com.example.mirea_testing.exception;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String username) {
        super("User with such username already exists: " + username);
    }
}
