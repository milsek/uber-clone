package com.example.springbackend.controller;

import com.example.springbackend.dto.display.AccountDisplayDTO;
import com.example.springbackend.dto.update.UserUpdateDTO;
import com.example.springbackend.dto.display.SessionDisplayDTO;
import com.example.springbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/account")
    public ResponseEntity<AccountDisplayDTO> getAccount(Authentication auth) {
        return ResponseEntity.ok(userService.   getAccount(auth));
    }

    @PostMapping("/update")
    public ResponseEntity<Boolean> updateUser(@RequestBody UserUpdateDTO userUpdateDTO){
        boolean successfulUpdate = userService.updateUser(userUpdateDTO);
        HttpStatus returnStatus = successfulUpdate ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(successfulUpdate, returnStatus);
    }

    @GetMapping(value = "/whoami")
    @PreAuthorize("hasAnyRole('DRIVER', 'PASSENGER', 'ADMIN')")
    public ResponseEntity<SessionDisplayDTO> whoAmI(Authentication auth) {
        return ResponseEntity.ok(userService.whoAmI(auth));
    }

}
