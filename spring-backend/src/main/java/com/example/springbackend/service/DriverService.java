package com.example.springbackend.service;

import com.example.springbackend.dto.creation.DriverCreationDTO;
import com.example.springbackend.dto.display.DriverDisplayDTO;
import com.example.springbackend.exception.UserAlreadyExistsException;
import com.example.springbackend.model.AccountStatus;
import com.example.springbackend.model.Driver;
import com.example.springbackend.model.Vehicle;
import com.example.springbackend.model.helpClasses.AuthenticationProvider;
import com.example.springbackend.repository.DriverRepository;
import com.example.springbackend.repository.VehicleRepository;
import com.example.springbackend.repository.VehicleTypeRepository;
import com.example.springbackend.util.TokenUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class DriverService {
    @Autowired
    private DriverRepository driverRepository;
    @Autowired
    VehicleRepository vehicleRepository;
    @Autowired
    VehicleTypeRepository vehicleTypeRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;
    @Autowired
    private TokenUtils tokenUtils;
    @Autowired
    private RoleService roleService;

    public Driver signUp(DriverCreationDTO driverCreationDTO) {
        if(!userService.userExistsForCustomRegistration(driverCreationDTO.getEmail(), driverCreationDTO.getUsername())) {
            Driver driver = createDriverFromDto(driverCreationDTO);
            Vehicle vehicle = createVehicleFromDto(driverCreationDTO);
            vehicleRepository.save(vehicle);
            driver.setVehicle(vehicle);
            driverRepository.save(driver);

            String jwt = tokenUtils.generateConfirmationToken(driverCreationDTO.getUsername());
            emailService.sendRegistrationEmail(driver, jwt);
            return driver;
        }
        else {
            throw new UserAlreadyExistsException();
        }
    }

    public DriverDisplayDTO getByUsername(String username) {
        Driver driver = driverRepository.findByUsername(username).orElseThrow();
        return modelMapper.map(driver, DriverDisplayDTO.class);
    }

    public void toggleActivity(Authentication auth) {
        Driver driver = (Driver) auth.getPrincipal();
        driver.setActive(!driver.getActive());
        driverRepository.save(driver);
    }

    public boolean getActivity(Authentication auth) {
        Driver driver = (Driver) auth.getPrincipal();
        return driver.getActive();
    }


    private Driver createDriverFromDto(DriverCreationDTO dto) {
        Driver driver = modelMapper.map(dto, Driver.class);
        driver.setAuthenticationProvider(AuthenticationProvider.LOCAL);
        driver.setPassword(passwordEncoder.encode(dto.getPassword()));
        driver.setRoles(roleService.findByName("ROLE_DRIVER"));
        driver.setActive(false);
        driver.setDistanceTravelled(0);
        driver.setRidesCompleted(0);
        driver.setNumberOfReviews(0);
        driver.setTotalRatingSum(0);
        driver.setCurrentRide(null);
        driver.setNextRide(null);
        driver.setAccountStatus(AccountStatus.WAITING);
        return driver;
    }

    private Vehicle createVehicleFromDto(DriverCreationDTO dto) {
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleType(vehicleTypeRepository.findByName(dto.getVehicleType()).orElseThrow());
        vehicle.setColour(dto.getColour());
        vehicle.setModel(dto.getModel());
        vehicle.setMake(dto.getMake());
        vehicle.setExpectedTripTime(0);
        vehicle.setBabySeat(dto.getBabySeat());
        vehicle.setPetsAllowed(dto.getPetsAllowed());
        vehicle.setLicensePlateNumber(dto.getLicensePlateNumber());
        Random rand = new Random();
        int i = rand.nextInt(0, TestDataSupplierService.locations.size());
        vehicle.setCurrentCoordinates(TestDataSupplierService.locations.get(i));
        vehicle.setRideActive(false);
        return vehicle;
    }
}
