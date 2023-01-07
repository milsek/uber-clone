package com.example.springbackend.service;

import com.example.springbackend.model.*;
import com.example.springbackend.model.helpClasses.Coordinates;
import com.example.springbackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
public class TestDataSupplierService {
    @Autowired
    DriverRepository driverRepository;
    @Autowired
    PassengerRepository passengerRepository;
    @Autowired
    AdminRepository adminRepository;
    @Autowired
    VehicleTypeRepository vehicleTypeRepository;
    @Autowired
    VehicleRepository vehicleRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    RideRepository rideRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public static List<Coordinates> locations = Arrays.asList(
            new Coordinates(45.241805, 19.798567),
            new Coordinates(45.245749, 19.851122),
            new Coordinates(45.252782, 19.855517),
            new Coordinates(45.264056, 19.829546),
            new Coordinates(45.257006, 19.801482)
    );

    @Transactional
    public void injectTestData() {
        addAdmins();
        addUsers();
        addDrivers();
    }

    private void addAdmins() {
        Admin admin = new Admin();
        admin.setUsername("admin1@noemail.com");
        admin.setEmail("admin1@noemail.com");
        admin.setPassword(passwordEncoder.encode("cascaded"));
        admin.setName("Commissioner");
        admin.setSurname("Gibert");
        admin.setPhoneNumber("+2624035735");
        admin.setCity("Marseille");
        admin.setRoles(roleRepository.findByName("ROLE_ADMIN"));
        adminRepository.save(admin);
    }

    private void addUsers() {
        Passenger passenger = new Passenger();
        passenger.setUsername("passenger1@noemail.com");
        passenger.setEmail("passenger1@noemail.com");
        passenger.setPassword(passwordEncoder.encode("cascaded"));
        passenger.setName("Francois");
        passenger.setSurname("Memphis");
        passenger.setPhoneNumber("+25346014691");
        passenger.setCity("Marseille");
        passenger.setAccountStatus(AccountStatus.ACTIVE);
        passenger.setDistanceTravelled(79.28);
        passenger.setRidesCompleted(28);
        passenger.setRoles(roleRepository.findByName("ROLE_PASSENGER"));
        passenger.setTokenBalance(690);
        passengerRepository.save(passenger);
    }

    private void addDrivers() {
//        addOtherDrivers();
        Vehicle vehicle = new Vehicle();
        vehicle.setBabySeat(false);
        vehicle.setPetsAllowed(true);
        vehicle.setMake("Checker");
        vehicle.setModel("Marathon A11");
        vehicle.setColour("Yellow");
        vehicle.setLicensePlateNumber("A31216");
        vehicle.setRideActive(true);
        Random rand = new Random();
        int i = rand.nextInt(0, locations.size());
        vehicle.setCurrentCoordinates(locations.get(i));
        vehicle.setNextCoordinates(locations.get((i+1) % locations.size()));
        vehicle.setCoordinatesChangedAt(LocalDateTime.now());
        vehicle.setExpectedTripTime(540);
        vehicle.setVehicleType(vehicleTypeRepository.findByName("COUPE").orElseThrow());
        vehicleRepository.save(vehicle);
        Driver driver = new Driver();
        driver.setUsername("travis");
        driver.setEmail("travis@noemail.com");
        driver.setPassword(passwordEncoder.encode("cascaded"));
        driver.setName("Travis");
        driver.setSurname("Bickle");
        driver.setPhoneNumber("+1 422 135 12");
        driver.setCity("New York City");
        driver.setActive(true);
        driver.setVehicle(vehicle);
        driver.setDistanceTravelled(5251.12);
        driver.setRidesCompleted(2153);
        driver.setTotalRatingSum(7814);
        driver.setNumberOfReviews(1693);
        driver.setAccountStatus(AccountStatus.ACTIVE);
        driver.setRoles(roleRepository.findByName("ROLE_DRIVER"));
        Ride mockRide = new Ride();
        rideRepository.save(mockRide);
        driver.setCurrentRide(mockRide);
        driver.setNextRide(null);
        driverRepository.save(driver);
    }

    private void addOtherDrivers() {
        Random rand = new Random();
        for (int i = 0; i < 5; i++) {
            Vehicle vehicle = new Vehicle();
            vehicle.setBabySeat(true);
            vehicle.setPetsAllowed(true);
            vehicle.setMake("Peugeot");
            vehicle.setModel("406");
            vehicle.setColour("Gray");
            vehicle.setLicensePlateNumber("A61353");
            vehicle.setRideActive(false);
            int k = rand.nextInt(0, locations.size());
            vehicle.setCurrentCoordinates(locations.get(k));
            vehicle.setNextCoordinates(locations.get(k));
            vehicle.setCoordinatesChangedAt(LocalDateTime.now());
            vehicle.setVehicleType(vehicleTypeRepository.findByName("COUPE").orElseThrow());
            vehicleRepository.save(vehicle);
            Driver driver = new Driver();
            driver.setUsername("driver" + i);
            driver.setEmail("driver" + i + "@noemail.com");
            driver.setPassword(passwordEncoder.encode("cascaded"));
            driver.setName("Driver" + i);
            driver.setSurname("Drivich");
            driver.setPhoneNumber("+1 422 135 12");
            driver.setCity("Novi Sad");
            driver.setActive(true);
            driver.setVehicle(vehicle);
            driver.setDistanceTravelled(5251.12 + i);
            driver.setRidesCompleted(2153 + i);
            driver.setTotalRatingSum(7814 + i);
            driver.setNumberOfReviews(1693 + i);
            driver.setRoles(roleRepository.findByName("ROLE_DRIVER"));
            driver.setCurrentRide(null);
            driver.setNextRide(null);
            driverRepository.save(driver);
        }
    }

}
