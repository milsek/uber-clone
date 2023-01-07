package com.example.springbackend.controller;

import com.example.springbackend.dto.creation.BasicRideCreationDTO;
import com.example.springbackend.dto.display.RideSimpleDisplayDTO;
import com.example.springbackend.service.RideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/api/rides", produces = MediaType.APPLICATION_JSON_VALUE)
public class RideController {
    @Autowired
    RideService rideService;

    @PostMapping("/basic")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<RideSimpleDisplayDTO> orderBasicRide(@Valid @RequestBody BasicRideCreationDTO dto, Authentication auth) {
        return ResponseEntity.ok(rideService.orderBasicRide(dto, auth));
    }
}
