package com.example.springbackend.exception;

public class PassengerAlreadyHasAnActiveRideException extends RuntimeException {
    public PassengerAlreadyHasAnActiveRideException() {
    }

    public PassengerAlreadyHasAnActiveRideException(String message) {
        super(message);
    }

    public PassengerAlreadyHasAnActiveRideException(String message, Throwable cause) {
        super(message, cause);
    }
}
