package com.example.springbackend.exception;

public class TooManyPassengersException extends RuntimeException {
    public TooManyPassengersException() {
    }

    public TooManyPassengersException(String message) {
        super(message);
    }

    public TooManyPassengersException(String message, Throwable cause) {
        super(message, cause);
    }
}
