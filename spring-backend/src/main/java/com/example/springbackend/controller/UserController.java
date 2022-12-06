package com.example.springbackend.controller;

import com.example.springbackend.dto.display.AccountDisplayDTO;
import com.example.springbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/account")
    public ResponseEntity<AccountDisplayDTO> getAccount(Authentication auth) {
        return ResponseEntity.ok(userService.getAccount(auth));
    }
}
