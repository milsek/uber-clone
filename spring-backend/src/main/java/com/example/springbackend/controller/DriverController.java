package com.example.springbackend.controller;

import com.example.springbackend.dto.display.DriverDisplayDTO;
import com.example.springbackend.service.DriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/drivers", produces = MediaType.APPLICATION_JSON_VALUE)
public class DriverController {
    @Autowired
    private DriverService driverService;

    @GetMapping("/{username}")
    public ResponseEntity<DriverDisplayDTO> getByUsername(@PathVariable String username) {
        return ResponseEntity.ok(driverService.getByUsername(username));
    }
}
