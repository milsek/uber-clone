package com.example.springbackend.controller;

import com.example.springbackend.dto.display.DriverDisplayDTO;
import com.example.springbackend.service.DriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/drivers", produces = MediaType.APPLICATION_JSON_VALUE)
public class DriverController {
    @Autowired
    private DriverService driverService;

    @GetMapping("/{username}")
    @PreAuthorize("hasAnyRole('DRIVER', 'PASSENGER', 'ADMIN')")
    public ResponseEntity<DriverDisplayDTO> getByUsername(@PathVariable String username) {
        return ResponseEntity.ok(driverService.getByUsername(username));
    }

    @GetMapping ("/activity")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Boolean> getActivity(Authentication auth) {
        return ResponseEntity.ok(driverService.getActivity(auth));
    }

    @PatchMapping ("/activity")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Void> getByUsername(Authentication auth) {
        driverService.toggleActivity(auth);
        return ResponseEntity.ok().build();
    }

}
