package com.example.springbackend.controller;

import com.example.springbackend.dto.display.RideSimpleDisplayDTO;
import com.example.springbackend.service.PassengerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/passengers", produces = MediaType.APPLICATION_JSON_VALUE)
public class PassengerController {
    @Autowired
    PassengerService passengerService;

    @GetMapping("/current-ride")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<RideSimpleDisplayDTO> getActivity(Authentication auth) {
        return ResponseEntity.ok(passengerService.getCurrentRide(auth));
    }
}
