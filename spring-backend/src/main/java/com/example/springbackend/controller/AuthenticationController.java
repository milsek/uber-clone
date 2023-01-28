package com.example.springbackend.controller;

import com.example.springbackend.dto.JwtAuthenticationRequestDTO;
import com.example.springbackend.dto.update.EmailDTO;
import com.example.springbackend.dto.update.PasswordResetDTO;
import com.example.springbackend.model.User;
import com.example.springbackend.security.UserTokenState;
import com.example.springbackend.service.*;
import com.example.springbackend.util.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping(value = "/api/auth")
public class AuthenticationController {

    @Autowired
    private TokenUtils tokenUtils;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private PassengerService passengerService;
    @Autowired
    private MemberService memberService;

    @PostMapping("/custom-login")
    public ResponseEntity<UserTokenState> createAuthenticationToken(
            @RequestBody JwtAuthenticationRequestDTO authenticationRequest) {
        try{
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authenticationRequest.getUsername(), authenticationRequest.getPassword()));
             SecurityContextHolder.getContext().setAuthentication(authentication);
            User user = (User) authentication.getPrincipal();
            if(!memberService.isUserActive(user.getUsername())){
                return null;
            }
            String jwt = tokenUtils.generateToken(user.getUsername());
            int expiresIn = tokenUtils.getExpiredIn();
            passengerService.getLoggedUser();
            return ResponseEntity.ok(new UserTokenState(jwt, expiresIn));
        } catch (AuthenticationException ae) {
            return null;
        }
    }

    @GetMapping("/confirm-registration/{token}")
    public ResponseEntity<String> confirmRegistration(@PathVariable String token) {
        memberService.confirmRegistration(token);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("http://localhost:4200/login"))
                .build();
    }

    @GetMapping("/auth-login/{token}")
    public ResponseEntity<String> authLogin(@PathVariable String token) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("http://localhost:4200/auth/google-login?token="+token))
                .build();
    }

    @PostMapping("/confirm-password-reset")
    public boolean confirmPasswordReset(@RequestBody PasswordResetDTO passwordResetDTO){
        return memberService.confirmPasswordReset(passwordResetDTO.getToken(),passwordResetDTO.getNewPassword());
    }

    @PostMapping("/reset-password")
    public boolean resetPassword(@RequestBody EmailDTO emailDTO){
        return memberService.passwordReset(emailDTO.getEmail());
    }
}
