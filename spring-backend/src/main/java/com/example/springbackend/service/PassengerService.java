package com.example.springbackend.service;

import com.example.springbackend.dto.creation.UserCreationDTO;
import com.example.springbackend.model.AccountStatus;
import com.example.springbackend.dto.display.RideSimpleDisplayDTO;
import com.example.springbackend.model.Driver;
import com.example.springbackend.model.Passenger;
import com.example.springbackend.model.Ride;
import com.example.springbackend.model.helpClasses.AuthenticationProvider;
import com.example.springbackend.repository.DriverRepository;
import com.example.springbackend.repository.PassengerRepository;
import com.example.springbackend.util.TokenUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.springbackend.repository.PassengerRideRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PassengerService {

    @Autowired
    private PassengerRepository passengerRepository;
    @Autowired
    private DriverRepository driverRepository;
    @Autowired
    private PassengerRideRepository passengerRideRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleService roleService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private TokenUtils tokenUtils;
    @Autowired
    private RideService rideService;

    public Passenger signUp(UserCreationDTO userCreationDTO) {
        if(!userService.userExistsForCustomRegistration(userCreationDTO.getEmail(), userCreationDTO.getUsername())){
            Passenger passenger = modelMapper.map(userCreationDTO, Passenger.class);
            passenger.setAuthenticationProvider(AuthenticationProvider.LOCAL);
            passenger.setPassword(passwordEncoder.encode(userCreationDTO.getPassword()));
            passenger.setRoles(roleService.findByName("ROLE_PASSENGER"));
            passenger.setAccountStatus(AccountStatus.WAITING);
            passenger.setTokenBalance(0);
            passengerRepository.save(passenger);
            String jwt = tokenUtils.generateConfirmationToken(userCreationDTO.getUsername());
            emailService.sendRegistrationEmail(passenger, jwt);
            return passenger;
        }
        return null;
    }

    public void getLoggedUser(){
        System.out.println(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    public void addToTokenBalance(int balance, String username) {
        Passenger passenger = passengerRepository.findByUsername(username).get();
        passenger.setTokenBalance(passenger.getTokenBalance() + balance);
        passengerRepository.save(passenger);
    }

    public RideSimpleDisplayDTO getCurrentRide(Authentication auth) {
        Passenger passenger = (Passenger) auth.getPrincipal();
        Ride currentRide = passengerRideRepository.getCurrentRide(passenger).orElseThrow();
        Driver driver = driverRepository.getDriverForRide(currentRide).orElseThrow();

        RideSimpleDisplayDTO rideDisplayDTO = rideService.createBasicRideSimpleDisplayDTO(currentRide, driver);
        return rideDisplayDTO;
    }
}
