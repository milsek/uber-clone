package com.example.springbackend.exception;

public class RideDoesNotBelongToPassengerException extends RuntimeException {
    public RideDoesNotBelongToPassengerException() {
    }

    public RideDoesNotBelongToPassengerException(String message) {
        super(message);
    }

    public RideDoesNotBelongToPassengerException(String message, Throwable cause) {
        super(message, cause);
    }
}
