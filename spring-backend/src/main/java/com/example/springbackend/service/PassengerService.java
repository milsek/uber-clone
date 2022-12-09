package com.example.springbackend.service;

import com.example.springbackend.dto.creation.UserCreationDTO;
import com.example.springbackend.model.Passenger;
import com.example.springbackend.model.User;
import com.example.springbackend.model.helpClasses.AuthenticationProvider;
import com.example.springbackend.model.security.CustomOAuth2User;
import com.example.springbackend.repository.PassengerRepository;
import com.example.springbackend.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class PassengerService {

    @Autowired
    private PassengerRepository passengerRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleService roleService;

    public Passenger signUp(UserCreationDTO userCreationDTO) {
        if(!userService.userExistsForCustomRegistration(userCreationDTO.getEmail(), userCreationDTO.getUsername())){
            Passenger passenger = modelMapper.map(userCreationDTO, Passenger.class);
            passenger.setAuthenticationProvider(AuthenticationProvider.LOCAL);
            passenger.setPassword(passwordEncoder.encode(userCreationDTO.getPassword()));
            passenger.setRoles(roleService.findByName("ROLE_PASSENGER"));
            passenger.setBlocked(false);
            passengerRepository.save(passenger);
            return passenger;
        }
        return null;
    }

    public void getLoggedUser(){
        System.out.println(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }
}
