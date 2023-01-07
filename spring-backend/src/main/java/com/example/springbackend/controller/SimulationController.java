package com.example.springbackend.controller;


import com.example.springbackend.service.SimulatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/simulator", produces = MediaType.APPLICATION_JSON_VALUE)
public class SimulationController {

    @Autowired
    SimulatorService simulatorService;

    @PostMapping("")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Void> simulateMove(Authentication auth) {
        simulatorService.simulateMove(auth);
        return ResponseEntity.ok().build();
    }
}
