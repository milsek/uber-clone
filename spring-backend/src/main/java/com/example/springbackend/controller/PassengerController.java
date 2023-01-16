package com.example.springbackend.controller;

import com.example.springbackend.dto.creation.UserCreationDTO;
import com.example.springbackend.dto.display.RideSimpleDisplayDTO;
import com.example.springbackend.model.Passenger;
import com.example.springbackend.service.PassengerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/api/passengers", produces = MediaType.APPLICATION_JSON_VALUE)
public class PassengerController {
    @Autowired
    PassengerService passengerService;

    @PostMapping("")
    public ResponseEntity<Passenger> signupPassenger(@Valid @RequestBody UserCreationDTO userCreationDTO) {
        Passenger passenger = passengerService.signUp(userCreationDTO);
        return new ResponseEntity<>(passenger, HttpStatus.CREATED);
    }

    @GetMapping("/current-ride")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<RideSimpleDisplayDTO> getActivity(Authentication auth) {
        return ResponseEntity.ok(passengerService.getCurrentRide(auth));
    }
}
