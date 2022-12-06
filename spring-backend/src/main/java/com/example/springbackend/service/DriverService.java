package com.example.springbackend.service;

import com.example.springbackend.dto.creation.UserCreationDTO;
import com.example.springbackend.dto.display.DriverDisplayDTO;
import com.example.springbackend.dto.display.UserDisplayDTO;
import com.example.springbackend.model.Driver;
import com.example.springbackend.model.Passenger;
import com.example.springbackend.model.User;
import com.example.springbackend.repository.DriverRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class DriverService {
    @Autowired
    private DriverRepository driverRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;


    public Driver signUp(UserCreationDTO userCreationDTO) {
        if(!userService.userExists(userCreationDTO.getEmail())){
            Driver driver = modelMapper.map(userCreationDTO, Driver.class);
            driver.setPassword(passwordEncoder.encode(userCreationDTO.getPassword()));
            driverRepository.save(driver);
            return driver;
        }
        return null;
    }

    public DriverDisplayDTO getByUsername(String username) {
        Driver driver = driverRepository.findByUsername(username).orElseThrow();
        return modelMapper.map(driver, DriverDisplayDTO.class);
    }
}
