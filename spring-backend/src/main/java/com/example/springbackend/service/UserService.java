package com.example.springbackend.service;

import com.example.springbackend.model.Passenger;
import com.example.springbackend.model.User;
import com.example.springbackend.model.helpClasses.AuthenticationProvider;
import com.example.springbackend.model.security.CustomOAuth2User;
import com.example.springbackend.repository.PassengerRepository;
import com.example.springbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    PassengerRepository passengerRepository;

    public Boolean userExists(String email){
        return userRepository.findByEmail(email).isPresent();
    }

    public void processOAuthPostLogin(CustomOAuth2User customOAuth2User) {
        if (!userExists(customOAuth2User.getEmail())) {
            Passenger newUser = new Passenger();
            newUser.setEmail(customOAuth2User.getEmail());
            newUser.setUsername(customOAuth2User.getEmail().split("@")[0]);
            newUser.setAuthenticationProvider(AuthenticationProvider.valueOf(customOAuth2User.getOauth2ClientName().toUpperCase()));
            passengerRepository.save(newUser);
        }
    }
}
