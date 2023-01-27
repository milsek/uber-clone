package com.example.springbackend.exception;

import com.example.springbackend.exception.error.ExceptionResponseBody;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class ControllerAdvisor {
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoSuchElementException.class)
    public ExceptionResponseBody handleNoSuchElementException(NoSuchElementException ex) {
        return new ExceptionResponseBody(
                HttpStatus.NOT_FOUND.value(),
                "Requested resource does not exist.");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ExceptionResponseBody handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        return new ExceptionResponseBody(
                HttpStatus.BAD_REQUEST.value(),
                "Username or email already exists.");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UserDoesNotExistException.class)
    public ExceptionResponseBody handleUserDoesNotExistException(UserDoesNotExistException ex) {
        return new ExceptionResponseBody(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(LinkedPassengersNotAllDistinctException.class)
    public ExceptionResponseBody handleLinkedPassengersNotAllDistinctException(LinkedPassengersNotAllDistinctException ex) {
        return new ExceptionResponseBody(
                HttpStatus.BAD_REQUEST.value(),
                "Not all linked passengers are distinct.");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ReservationTooSoonException.class)
    public ExceptionResponseBody handleReservationTooSoonException(ReservationTooSoonException ex) {
        return new ExceptionResponseBody(
                HttpStatus.BAD_REQUEST.value(),
                "Reservation must be made at least 20 minutes in advance.");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RideDoesNotBelongToPassengerException.class)
    public ExceptionResponseBody handleRideDoesNotBelongToPassengerException(RideDoesNotBelongToPassengerException ex) {
        return new ExceptionResponseBody(
                HttpStatus.BAD_REQUEST.value(),
                "Ride does not belong to request sender.");
    }

    @ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
    @ExceptionHandler(InsufficientFundsException.class)
    public ExceptionResponseBody handleInsufficientFundsException(InsufficientFundsException ex) {
        return new ExceptionResponseBody(
                HttpStatus.PAYMENT_REQUIRED.value(),
                "Insufficient funds.");
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(AdequateDriverNotFoundException.class)
    public ExceptionResponseBody handleAdequateDriverNotFoundException(AdequateDriverNotFoundException ex) {
        return new ExceptionResponseBody(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Adequate driver not found.");
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(PassengerAlreadyHasAnActiveRideException.class)
    public ExceptionResponseBody handlePassengerAlreadyHasActiveRideException(PassengerAlreadyHasAnActiveRideException ex) {
        return new ExceptionResponseBody(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Passenger already has an active ride.");
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(TooManyPassengersException.class)
    public ExceptionResponseBody handleTooManyPassengersException(TooManyPassengersException ex) {
        return new ExceptionResponseBody(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Number of passengers exceeds vehicle capacity.");
    }
}
