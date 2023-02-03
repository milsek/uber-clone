package com.example.springbackend.service;

import com.example.springbackend.model.*;
import com.example.springbackend.model.helpClasses.Coordinates;
import com.example.springbackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    RouteRepository routeRepository;
    @Autowired
    RideRepository rideRepository;
    @Autowired
    PassengerRideRepository passengerRideRepository;
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
        addRides();
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
        admin.setProfilePicture("/admin1@noemail.com.png");
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
        passenger.setDistanceTravelled(5.00);
        passenger.setRidesCompleted(1);
        passenger.setRoles(roleRepository.findByName("ROLE_PASSENGER"));
        passenger.setTokenBalance(1000);
        passenger.setProfilePicture("/passenger1@noemail.com.png");
        passengerRepository.save(passenger);
        passenger.setTokenBalance(800);
        passenger.setDistanceTravelled(0.0);
        passenger.setRidesCompleted(0);
        passenger.setName("Karl Gustav");
        passenger.setSurname("Jung");
        passenger.setUsername("passenger2@noemail.com");
        passenger.setEmail("passenger2@noemail.com");
        passenger.setProfilePicture("/default.png");
        passengerRepository.save(passenger);
        passenger.setName("Sigmund");
        passenger.setSurname("Freud");
        passenger.setUsername("passenger3@noemail.com");
        passenger.setEmail("passenger3@noemail.com");
        passenger.setProfilePicture("/default.png");
        passengerRepository.save(passenger);
        passenger.setName("Erich");
        passenger.setSurname("Fromm");
        passenger.setUsername("passenger4@noemail.com");
        passenger.setEmail("passenger4@noemail.com");
        passenger.setTokenBalance(690);
        passenger.setProfilePicture("/default.png");
        passengerRepository.save(passenger);
        passenger.setName("Carl");
        passenger.setSurname("Rogers");
        passenger.setUsername("passenger5@noemail.com");
        passenger.setEmail("passenger5@noemail.com");
        passenger.setTokenBalance(10);
        passenger.setProfilePicture("/default.png");
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
//        vehicle.setRideActive(true);
        vehicle.setRideActive(false);
//        Random rand = new Random();
//        int i = rand.nextInt(0, locations.size());
        vehicle.setCurrentCoordinates(locations.get(0));
//        vehicle.setNextCoordinates(locations.get((i+1) % locations.size()));
        vehicle.setNextCoordinates(locations.get(0));
        vehicle.setCoordinatesChangedAt(LocalDateTime.now());
        vehicle.setExpectedTripTime(0);
        vehicle.setVehicleType(vehicleTypeRepository.findByName("COUPE").orElseThrow());
        vehicleRepository.save(vehicle);
        Driver driver = new Driver();
        driver.setUsername("driver1@noemail.com");
        driver.setEmail("driver1@noemail.com");
        driver.setPassword(passwordEncoder.encode("cascaded"));
        driver.setName("Travis");
        driver.setSurname("Bickle");
        driver.setPhoneNumber("+142213512");
        driver.setCity("New York City");
        driver.setActive(true);
        driver.setVehicle(vehicle);
        driver.setDistanceTravelled(5.0);
        driver.setRidesCompleted(1);
        driver.setTotalRatingSum(4);
        driver.setNumberOfReviews(1);
        driver.setAccountStatus(AccountStatus.ACTIVE);
        driver.setRoles(roleRepository.findByName("ROLE_DRIVER"));
        driver.setProfilePicture("/driver1@noemail.com.jpeg");
//        Ride mockRide = new Ride();
//        mockRide.setCreatedAt(LocalDateTime.now());
//        mockRide.setStartTime(LocalDateTime.now());
//        mockRide.setEndTime(LocalDateTime.now());
//        rideRepository.save(mockRide);
//        driver.setCurrentRide(mockRide);
        driver.setCurrentRide(null);
        driver.setNextRide(null);
        driverRepository.save(driver);
//        driver.setUsername("driver2@noemail.com");
//        driverRepository.save(driver);
//        driver.setUsername("driver3@noemail.com");
//        driverRepository.save(driver);
//        driver.setUsername("driver4@noemail.com");
//        driverRepository.save(driver);
    }

    private void addRides(){
        List<Passenger> passengers = new ArrayList<>();
        passengers.add(passengerRepository.findByUsername("passenger1@noemail.com").get());
        passengers.add(passengerRepository.findByUsername("passenger2@noemail.com").get());
        Ride ride = new Ride();
        ride.setStartTime(LocalDateTime.now());
        ride.setEndTime(LocalDateTime.now().minusMonths(3));
        ride.setDriver(driverRepository.findByUsername("driver1@noemail.com").get());
        ride.setDistance(5.0);
        ride.setStartAddress("Djordja Niksica Johana 2, Bistrica, Novi Sad City");
        ride.setDestinationAddress("Bulevar Slobodana Jovanovića 18, Bistrica, Novi Sad City");
        ride.setVehicleType("COUPE");
        Route route = new Route();
        route.setPassengers(passengers);
        route.setCoordinates(new ArrayList<>(){{
            add(new Coordinates(45.25634, 19.80225));
            add(new Coordinates(45.25631, 19.80215));
            add(new Coordinates(45.25631, 19.80215));
            add(new Coordinates(45.25628, 19.80207));
            add(new Coordinates(45.2554, 19.80266));
            add(new Coordinates(45.25534, 19.8027));
            add(new Coordinates(45.25411, 19.80354));
            add(new Coordinates(45.25376, 19.80377));
            add(new Coordinates(45.25357, 19.80391));
            add(new Coordinates(45.25336, 19.80404));
            add(new Coordinates(45.25333, 19.80406));
            add(new Coordinates(45.2532, 19.80415));
            add(new Coordinates(45.25312, 19.8042));
            add(new Coordinates(45.25317, 19.80433));
            add(new Coordinates(45.25323, 19.80427));
            add(new Coordinates(45.25337, 19.80417));
            add(new Coordinates(45.25339, 19.80415));
            add(new Coordinates(45.25361, 19.804));
            add(new Coordinates(45.25366, 19.80397));
            add(new Coordinates(45.25366, 19.80397));
        }});
        route.setWaypoints(new ArrayList<>(){{
            add(new Coordinates(45.256336, 19.802247));
            add(new Coordinates(45.253658, 19.803968));
        }});
        routeRepository.save(route);
        ride.setRoute(route);
        ride.setPassengersConfirmed(true);
        ride.setCreatedAt(LocalDateTime.now().minusMonths(3));
        ride.setPrice(50);
        ride.setId(1);
        ride.setStatus(RideStatus.COMPLETED);
        rideRepository.save(ride);
        PassengerRide passengerRide = new PassengerRide();
        passengerRide.setRide(ride);
        passengerRide.setPassenger(passengerRepository.findByUsername("passenger1@noemail.com").get());
        passengerRide.setFare(80);
        passengerRide.setDriverRating(4);
        passengerRide.setVehicleRating(3);
        passengerRide.setComment("The car was old and dirty.");
        passengerRideRepository.save(passengerRide);
//        ride.setDriver(driverRepository.findByUsername("driver2@noemail.com").get());
//
//
//        ride.setId(2);
//        rideRepository.save(ride);
//        passengerRide.setRide(ride);
//        passengerRide.setPassenger(passengerRepository.findByUsername("passenger1@noemail.com").get());
//        passengerRide.setFare(60);
//        passengerRide.setId(2);
//        passengers.remove(1);
//        ride.setId(3);
//        ride.setPrice(90);
//        ride.setDistance(630.0);
//        ride.setStartTime(LocalDateTime.now().minusMonths(4));
//        ride.setDriver(driverRepository.findByUsername("driver1@noemail.com").get());
//        rideRepository.save(ride);
//        passengerRideRepository.save(passengerRide);
//
//
//        passengers.add(passengerRepository.findByUsername("passenger3@noemail.com").get());
//        passengerRide.setPassenger(passengerRepository.findByUsername("passenger3@noemail.com").get());
//        passengerRide.setFare(80);
//        passengerRide.setId(8);
//        passengerRideRepository.save(passengerRide);
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
            driver.setProfilePicture("/default.png");
            driverRepository.save(driver);
        }
    }

}
