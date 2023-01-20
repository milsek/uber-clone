package com.example.springbackend.exception;

public class LinkedPassengersNotAllDistinctException extends RuntimeException {
    public LinkedPassengersNotAllDistinctException() {
    }

    public LinkedPassengersNotAllDistinctException(String message) {
        super(message);
    }

    public LinkedPassengersNotAllDistinctException(String message, Throwable cause) {
        super(message, cause);
    }
}
