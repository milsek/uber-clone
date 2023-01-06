package com.example.springbackend.controller;

import com.example.springbackend.dto.JwtAuthenticationRequestDTO;
import com.example.springbackend.dto.creation.UserCreationDTO;
import com.example.springbackend.dto.update.UsernameDTO;
import com.example.springbackend.model.Driver;
import com.example.springbackend.model.Passenger;
import com.example.springbackend.model.User;
import com.example.springbackend.security.UserTokenState;
import com.example.springbackend.service.*;
import com.example.springbackend.util.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;
    @Autowired
    TestService testService;

    @PostMapping("/ban-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> banUser(@RequestBody UsernameDTO usernameDTO){
        adminService.banMember(usernameDTO.getUsername());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unban-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> unbanUser(@RequestBody UsernameDTO usernameDTO){
        adminService.unbanMember(usernameDTO.getUsername());
        return ResponseEntity.ok().build();
    }
}
