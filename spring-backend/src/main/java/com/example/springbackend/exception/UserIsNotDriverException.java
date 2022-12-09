package com.example.springbackend.exception;

public class UserIsNotDriverException extends RuntimeException {
    public UserIsNotDriverException() {
    }

    public UserIsNotDriverException(String message) {
        super(message);
    }

    public UserIsNotDriverException(String message, Throwable cause) {
        super(message, cause);
    }
}
