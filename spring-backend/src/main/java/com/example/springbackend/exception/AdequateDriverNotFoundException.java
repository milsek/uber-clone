package com.example.springbackend.exception;

public class AdequateDriverNotFoundException extends RuntimeException {
    public AdequateDriverNotFoundException() {
    }

    public AdequateDriverNotFoundException(String message) {
        super(message);
    }

    public AdequateDriverNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
