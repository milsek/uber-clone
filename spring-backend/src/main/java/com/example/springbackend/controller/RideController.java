package com.example.springbackend.controller;

import com.example.springbackend.dto.creation.BasicRideCreationDTO;
import com.example.springbackend.dto.creation.ReviewDTO;
import com.example.springbackend.dto.creation.RideIdDTO;
import com.example.springbackend.dto.creation.SplitFareRideCreationDTO;
import com.example.springbackend.dto.display.DetailedRideHistoryDriverDTO;
import com.example.springbackend.dto.display.DetailedRideHistoryPassengerDTO;
import com.example.springbackend.dto.display.RideHistoryDisplayDTO;
import com.example.springbackend.dto.display.RideSimpleDisplayDTO;
import com.example.springbackend.dto.update.UsernameDTO;
import com.example.springbackend.service.RideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

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

    @PostMapping("/split-fare")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<Boolean> orderSplitFareRide(@Valid @RequestBody SplitFareRideCreationDTO dto, Authentication auth) {
        return ResponseEntity.ok(rideService.orderSplitFareRide(dto, auth));
    }

    @PostMapping("/confirm-ride")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<Object> confirmRide(@Valid @RequestBody RideIdDTO dto, Authentication auth) {
        return ResponseEntity.ok(rideService.confirmRide(dto, auth));
    }

    @GetMapping("/ride-history")
    public Page<RideHistoryDisplayDTO> getRideHistory(@Valid @RequestParam String username, @RequestParam Integer page, @RequestParam Integer amount, @RequestParam String sortBy, Authentication authentication){
        Pageable paging = PageRequest.of(page, amount, Sort.by(sortBy));
        return rideService.getRideHistory(username, authentication, paging);
    }


    @GetMapping("/detailed-ride-history-passenger")
    @PreAuthorize("hasAnyRole('PASSENGER', 'ADMIN')")
    public DetailedRideHistoryPassengerDTO detailedRideHistoryPassenger(@Valid @RequestParam Integer rideId, Authentication authentication){
        return rideService.detailedRideHistoryPassenger(rideId, authentication);
    }

    @GetMapping("/detailed-ride-history-driver")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    public DetailedRideHistoryDriverDTO detailedRideHistoryDriver(@Valid @RequestParam Integer rideId, Authentication authentication){
        return rideService.detailedRideHistoryDriver(rideId, authentication);
    }

    @PostMapping("/leave-review")
    public ResponseEntity<Boolean> leaveReview(@Valid @RequestBody ReviewDTO reviewDTO, Authentication authentication ){
        return ResponseEntity.ok(rideService.leaveReview(reviewDTO, authentication));
    }
}
