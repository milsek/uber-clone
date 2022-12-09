package com.example.springbackend.controller;

import com.example.springbackend.dto.display.DriverDisplayDTO;
import com.example.springbackend.service.DriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/drivers", produces = MediaType.APPLICATION_JSON_VALUE)
public class DriverController {
    @Autowired
    private DriverService driverService;

    @GetMapping("/{username}")
    public ResponseEntity<DriverDisplayDTO> getByUsername(@PathVariable String username) {
        return ResponseEntity.ok(driverService.getByUsername(username));
    }

    @GetMapping ("/activity")
    public ResponseEntity<Boolean> getActivity(Authentication auth) {
        driverService.getActivity(auth);
        return ResponseEntity.ok(driverService.getActivity(auth));
    }

    @PatchMapping ("/activity")
    public ResponseEntity<Void> getByUsername(Authentication auth) {
        driverService.toggleActivity(auth);
        return ResponseEntity.ok().build();
    }
}
