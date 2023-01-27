package com.example.springbackend.service;

import com.example.springbackend.dto.creation.UserCreationDTO;
import com.example.springbackend.dto.display.*;
import com.example.springbackend.dto.search.SearchDTO;
import com.example.springbackend.exception.UserAlreadyExistsException;
import com.example.springbackend.model.*;
import com.example.springbackend.model.helpClasses.AuthenticationProvider;
import com.example.springbackend.repository.DriverRepository;
import com.example.springbackend.repository.PassengerRepository;
import com.example.springbackend.util.TokenUtils;
import com.example.springbackend.websocket.MessageType;
import com.example.springbackend.websocket.WSMessage;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.springbackend.repository.PassengerRideRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
            passenger.setProfilePicture("/default.png");
            passengerRepository.save(passenger);
            String jwt = tokenUtils.generateConfirmationToken(userCreationDTO.getUsername());
            emailService.sendRegistrationEmail(passenger, jwt);
            return passenger;
        }
        else {
            throw new UserAlreadyExistsException();
        }
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
        PassengerRide currentPassengerRide = passengerRideRepository.getCurrentPassengerRide(passenger).orElseThrow();
        Optional<Driver> optionalDriver = driverRepository.getDriverForRide(currentPassengerRide.getRide());
        Driver driver = optionalDriver.isPresent() ? optionalDriver.get() : null;
        RideSimpleDisplayDTO rideDisplayDTO = this.rideService.createBasicRideSimpleDisplayDTO(currentPassengerRide, driver);
        return rideDisplayDTO;
    }

    public PassengerSearchDisplayDTO searchPassengers(SearchDTO searchDTO) {
        List<Passenger> passengers = passengerRepository.searchPassengers(searchDTO.getName(), searchDTO.getSurname(), searchDTO.getUsername());
        PassengerSearchDisplayDTO passengerSearchDisplayDTO = new PassengerSearchDisplayDTO();
        passengerSearchDisplayDTO.setPassengers(passengers.subList(searchDTO.getPage()*7, Math.min((searchDTO.getPage()+1)*7, passengers.size())));
        passengerSearchDisplayDTO.setNumberOfPassengers(passengers.size());
        return passengerSearchDisplayDTO;
    }
}
