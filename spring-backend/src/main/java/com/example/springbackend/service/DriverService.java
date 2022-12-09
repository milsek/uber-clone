package com.example.springbackend.service;

import com.example.springbackend.dto.creation.UserCreationDTO;
import com.example.springbackend.dto.display.DriverDisplayDTO;
import com.example.springbackend.exception.UserIsNotDriverException;
import com.example.springbackend.model.Driver;
import com.example.springbackend.model.User;
import com.example.springbackend.model.helpClasses.AuthenticationProvider;
import com.example.springbackend.repository.DriverRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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
    @Autowired
    private RoleService roleService;


    public Driver signUp(UserCreationDTO userCreationDTO) {
        if(!userService.userExistsForCustomRegistration(userCreationDTO.getEmail(),userCreationDTO.getUsername())){
            Driver driver = modelMapper.map(userCreationDTO, Driver.class);
            driver.setAuthenticationProvider(AuthenticationProvider.LOCAL);
            driver.setPassword(passwordEncoder.encode(userCreationDTO.getPassword()));
            driver.setRoles(roleService.findByName("ROLE_DRIVER"));
            driverRepository.save(driver);
            return driver;
        }
        return null;
    }

    public DriverDisplayDTO getByUsername(String username) {
        Driver driver = driverRepository.findByUsername(username).orElseThrow();
        return modelMapper.map(driver, DriverDisplayDTO.class);
    }

    public void toggleActivity(Authentication auth) {
        User user = (User) auth.getPrincipal();
        if (user instanceof Driver) {
            Driver driver = (Driver) user;
            driver.setActive(!driver.getActive());
            driverRepository.save(driver);
        }
        else {
            throw new UserIsNotDriverException();
        }
    }

    public boolean getActivity(Authentication auth) {
        User user = (User) auth.getPrincipal();
        if (user instanceof Driver) {
            return ((Driver) user).getActive();
        }
        else {
            throw new UserIsNotDriverException();
        }
    }
}
