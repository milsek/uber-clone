package com.example.springbackend.controller;

import com.example.springbackend.dto.display.VehiclePositionDisplayDTO;
import com.example.springbackend.dto.display.VehicleTypeDisplayDTO;
import com.example.springbackend.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping(value = "/api/vehicles", produces = MediaType.APPLICATION_JSON_VALUE)
public class VehicleController {

    @Autowired
    VehicleService vehicleService;

    @GetMapping("/types")
    public ResponseEntity<Collection<VehicleTypeDisplayDTO>> getAllTypes() {
        return ResponseEntity.ok(vehicleService.getAllTypes());
    }

    @GetMapping("/positions")
    public ResponseEntity<Collection<VehiclePositionDisplayDTO>> getPositions() {
        return ResponseEntity.ok(vehicleService.getPositions());
    }
}
