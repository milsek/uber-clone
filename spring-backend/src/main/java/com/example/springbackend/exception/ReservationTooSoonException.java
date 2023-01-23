package com.example.springbackend.exception;

public class ReservationTooSoonException extends RuntimeException {
    public ReservationTooSoonException() {
    }

    public ReservationTooSoonException(String message) {
        super(message);
    }

    public ReservationTooSoonException(String message, Throwable cause) {
        super(message, cause);
    }
}
