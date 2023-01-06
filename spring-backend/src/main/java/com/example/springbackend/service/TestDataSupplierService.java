package com.example.springbackend.service;

import com.example.springbackend.model.*;
import com.example.springbackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;

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
    private PasswordEncoder passwordEncoder;

    @Transactional
    public void injectTestData() {
        addAdmins();
        addUsers();
        ArrayList<VehicleType> vehicleTypes = generateVehicleTypes();
        addDrivers(vehicleTypes);
    }

    private void addAdmins() {
        Admin admin = new Admin();
        admin.setUsername("admin1");
        admin.setEmail("admin1@noemail.com");
        admin.setPassword(passwordEncoder.encode("cascaded"));
        admin.setName("Commissioner");
        admin.setSurname("Gibert");
        admin.setPhoneNumber("+2624035735");
        admin.setCity("Marseille");
        adminRepository.save(admin);
    }

    private void addUsers() {
        Passenger passenger = new Passenger();
        passenger.setUsername("passenger1");
        passenger.setEmail("passenger1@noemail.com");
        passenger.setPassword(passwordEncoder.encode("cascaded"));
        passenger.setName("Francois");
        passenger.setSurname("Memphis");
        passenger.setPhoneNumber("+25346014691");
        passenger.setCity("Marseille");
        passenger.setBanned(false);
        passenger.setDistanceTravelled(79.28);
        passenger.setRidesCompleted(28);
        passengerRepository.save(passenger);
    }

    private void addDrivers(ArrayList<VehicleType> vehicleTypes) {
        Vehicle vehicle = new Vehicle();
        vehicle.setBabySeat(true);
        vehicle.setPetsAllowed(true);
        vehicle.setMake("Checker");
        vehicle.setModel("Marathon A11");
        vehicle.setColour("Yellow");
        vehicle.setLicensePlateNumber("A31216");
        vehicle.setVehicleType(vehicleTypes.get(0));
        vehicleRepository.save(vehicle);
        Driver driver = new Driver();
        driver.setUsername("driver1");
        driver.setEmail("driver1@noemail.com");
        driver.setPassword(passwordEncoder.encode("cascaded"));
        driver.setName("Travis");
        driver.setSurname("Bickle");
        driver.setPhoneNumber("+1 422 135 12");
        driver.setCity("New York City");
        driver.setActive(false);
        driver.setVehicle(vehicle);
        driver.setDistanceTravelled(5251.12);
        driver.setRidesCompleted(2153);
        driver.setTotalRatingSum(7814);
        driver.setNumberOfReviews(1693);
        driver.setBanned(false);
        driverRepository.save(driver);
    }
    private ArrayList<VehicleType> generateVehicleTypes() {
        ArrayList<VehicleType> vehicleTypes = new ArrayList<VehicleType>();
        VehicleType vt1 = new VehicleType();
        vt1.setPrice(1.535);
        vt1.setName("Coupe");
        vehicleTypes.add(vt1);
        vehicleTypeRepository.save(vt1);
        return vehicleTypes;
    }

}
