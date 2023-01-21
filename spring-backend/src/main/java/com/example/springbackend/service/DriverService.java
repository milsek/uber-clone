package com.example.springbackend.service;

import com.example.springbackend.dto.creation.DriverCreationDTO;
import com.example.springbackend.dto.display.DriverDisplayDTO;
import com.example.springbackend.exception.UserAlreadyExistsException;
import com.example.springbackend.model.AccountStatus;
import com.example.springbackend.dto.update.DriverUpdateDTO;
import com.example.springbackend.model.Driver;
import com.example.springbackend.model.Vehicle;
import com.example.springbackend.model.helpClasses.AuthenticationProvider;
import com.example.springbackend.repository.DriverRepository;
import com.example.springbackend.repository.VehicleRepository;
import com.example.springbackend.repository.VehicleTypeRepository;
import com.example.springbackend.util.TokenUtils;
import com.example.springbackend.websocket.MessageType;
import com.example.springbackend.websocket.WSMessage;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.List;
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
    @Autowired
    private PreupdateService preupdateService;

    private Map<String, ScheduledFuture<?>> currentThreads;

    private final SimpMessagingTemplate template;

    @Autowired
    public DriverService(SimpMessagingTemplate template) {
        this.currentThreads = new HashMap<>();
        this.template = template;
    }

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

    public boolean toggleActivity(Authentication auth) {
        Driver driver = (Driver) auth.getPrincipal();
        if(driver.getActiveMinutesToday() < 480){
            triggerActivity(driver);
            driver.setActive(!driver.getActive());
            driverRepository.save(driver);
            return true;
        }
        return false;
    }

    private void triggerActivity(Driver driver){
        if(driver.getActive()){
            if(currentThreads.get(driver.getUsername()) != null)
                currentThreads.get(driver.getUsername()).cancel(false);
            if(driver.getLastSetActive() != null){
                driver.setActiveMinutesToday(driver.getActiveMinutesToday() + Duration.between(driver.getLastSetActive(), LocalDateTime.now()).toMinutes());
            }
            driver.setLastSetActive(LocalDateTime.now());
            driverRepository.save(driver);
        }
        else{
            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            ScheduledFuture<?> future = executorService.schedule(() -> {
                driver.setActive(false);
                driverRepository.save(driver);
                WSMessage message = WSMessage.builder()
                        .type(MessageType.NOTIFICATION)
                        .sender("server")
                        .receiver(driver.getUsername())
                        .content("Your 8 hours has expired")
                        .sentDateTime(LocalDateTime.now())
                        .build();
                driver.setActiveMinutesToday(driver.getActiveMinutesToday() + Duration.between(driver.getLastSetActive(), LocalDateTime.now()).toMinutes());
                driverRepository.save(driver);
                this.template.convertAndSendToUser(driver.getUsername(), "/private/driver/overtime", message);

            }, 480-(int)driver.getActiveMinutesToday(), TimeUnit.MINUTES);
            currentThreads.put(driver.getUsername(), future);
        }
    }

    public boolean getActivity(Authentication auth) {
        Driver driver = (Driver) auth.getPrincipal();
        return driver.getActive();
    }

    @Scheduled(cron = "0 0 0 * * *")
    private void resetHours(){
        List<Driver> drivers = driverRepository.findAll();
        for(Driver d : drivers){
            d.setActiveMinutesToday(0);
            if(d.getActive()){
                triggerActivity(d);
            }
            else{
                d.setLastSetActive(null);
            }
            driverRepository.save(d);
        }
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
    public boolean updateDriver(DriverUpdateDTO driverUpdateDTO) {
        Optional<Driver> opetDriver = driverRepository.findByUsername(driverUpdateDTO.getUsername());
        if(opetDriver.isPresent()){
            Driver driver = opetDriver.get();
            driver.setCity(driverUpdateDTO.getCity());
            driver.setName(driverUpdateDTO.getName());
            driver.setSurname(driverUpdateDTO.getSurname());
            driver.setPhoneNumber(driverUpdateDTO.getPhoneNumber());
            driver.setProfilePicture(driverUpdateDTO.getProfilePicture().substring(4));
            Vehicle v = driver.getVehicle();
            v.setColour(driverUpdateDTO.getColour());
            v.setModel(driverUpdateDTO.getModel());
            v.setMake(driverUpdateDTO.getMake());
            v.setBabySeat(driverUpdateDTO.getBabySeat());
            v.setPetsAllowed(driverUpdateDTO.getPetsAllowed());
            v.setLicensePlateNumber(driverUpdateDTO.getLicensePlateNumber());
            v.setVehicleType(vehicleTypeRepository.findByName(driverUpdateDTO.getVehicleType()).get());
            driverRepository.save(driver);
            vehicleRepository.save(v);
            preupdateService.removeUpdateRequest(driverUpdateDTO);
            return true;
        }
        return false;
    }

}
