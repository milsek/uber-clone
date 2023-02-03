package com.example.springbackend.repository;

import com.example.springbackend.model.*;
import com.example.springbackend.model.helpClasses.Coordinates;
import com.example.springbackend.service.TestDataSupplierService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.test.context.ActiveProfiles;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Repository.class))
@ActiveProfiles("test")
public class RideRepositoryTests {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    RideRepository rideRepository;
    @Autowired
    PassengerRideRepository passengerRideRepository;
    @Autowired
    DriverRepository driverRepository;
    @Autowired
    VehicleTypeRepository vehicleTypeRepository;

    @MockBean
    TestDataSupplierService testDataSupplierService;


    // getRidesPendingRejection
    @Test
    void Return_rides_pending_rejection() {
        Ride ride1 = new Ride();
        ride1.setDriverRejectionReason("Reason 1");
        ride1.setStatus(RideStatus.DRIVER_ARRIVING);
        entityManager.persist(ride1);

        Ride ride2 = new Ride();
        ride2.setDriverRejectionReason("Reason 2");
        ride2.setStatus(RideStatus.DRIVER_ARRIVING);
        entityManager.persist(ride2);

        Ride ride3 = new Ride();
        ride3.setDriverRejectionReason(null);
        ride3.setStatus(RideStatus.DRIVER_ARRIVING);
        entityManager.persist(ride3);

        Ride ride4 = new Ride();
        ride3.setDriverRejectionReason("Reason 4");
        ride3.setStatus(RideStatus.IN_PROGRESS);
        entityManager.persist(ride4);

        entityManager.flush();

        List<Ride> ridesPendingRejection = rideRepository.getRidesPendingRejection();

        assertTrue(ridesPendingRejection.size() == 2);
        assertTrue(ridesPendingRejection
                .stream()
                .anyMatch(ride -> ride.getDriverRejectionReason().equals("Reason 1")));
        assertTrue(ridesPendingRejection
                .stream()
                .anyMatch(ride -> ride.getDriverRejectionReason().equals("Reason 2")));
    }

    // getCurrentPassengerRide
    @Test
    void Return_current_passenger_ride_for_passenger() {
        Passenger passenger1 = new Passenger();
        passenger1.setUsername("passenger@noemail.com");
        entityManager.persist(passenger1);
        Ride ride1 = new Ride();
        ride1.setStatus(RideStatus.DRIVER_ARRIVING);
        entityManager.persist(ride1);
        PassengerRide passengerRide1 = new PassengerRide();
        passengerRide1.setPassenger(passenger1);
        passengerRide1.setRide(ride1);
        entityManager.persist(passengerRide1);

        Ride ride2 = new Ride();
        ride2.setStatus(RideStatus.CANCELLED);
        entityManager.persist(ride2);
        PassengerRide passengerRide2 = new PassengerRide();
        passengerRide2.setPassenger(passenger1);
        passengerRide2.setRide(ride2);
        entityManager.persist(passengerRide2);

        entityManager.flush();

        Optional<PassengerRide> optionalPassengerRide = passengerRideRepository.getCurrentPassengerRide(passenger1);
        assertTrue(optionalPassengerRide.isPresent());
        assertEquals(optionalPassengerRide.get().getPassenger(), passenger1);
        assertEquals(optionalPassengerRide.get().getRide(), ride1);
    }

    @Test
    void Return_nothing_for_passenger_with_no_current_ride() {
        Passenger passenger1 = new Passenger();
        passenger1.setUsername("passenger@noemail.com");
        entityManager.persist(passenger1);
        Ride ride1 = new Ride();
        ride1.setStatus(RideStatus.CANCELLED);
        entityManager.persist(ride1);
        PassengerRide passengerRide1 = new PassengerRide();
        passengerRide1.setPassenger(passenger1);
        passengerRide1.setRide(ride1);
        entityManager.persist(passengerRide1);

        entityManager.flush();

        Optional<PassengerRide> optionalPassengerRide = passengerRideRepository.getCurrentPassengerRide(passenger1);
        assertFalse(optionalPassengerRide.isPresent());
    }

    @Test
    void Return_nothing_for_passenger_with_no_rides() {
        Passenger passenger1 = new Passenger();
        passenger1.setUsername("passenger@noemail.com");
        entityManager.persist(passenger1);

        entityManager.flush();

        Optional<PassengerRide> optionalPassengerRide = passengerRideRepository.getCurrentPassengerRide(passenger1);
        assertFalse(optionalPassengerRide.isPresent());
    }


    // getCurrentPassengerRidesByUsername
    @Test
    void Return_current_passenger_ride_for_passenger_by_username() {
        Passenger passenger1 = new Passenger();
        passenger1.setUsername("passenger1@noemail.com");
        entityManager.persist(passenger1);
        Ride ride1 = new Ride();
        ride1.setStatus(RideStatus.DRIVER_ARRIVING);
        entityManager.persist(ride1);
        PassengerRide passengerRide1 = new PassengerRide();
        passengerRide1.setPassenger(passenger1);
        passengerRide1.setRide(ride1);
        entityManager.persist(passengerRide1);
        Ride ride2 = new Ride();
        ride2.setStatus(RideStatus.CANCELLED);
        entityManager.persist(ride2);
        PassengerRide passengerRide2 = new PassengerRide();
        passengerRide2.setPassenger(passenger1);
        passengerRide2.setRide(ride2);
        entityManager.persist(passengerRide2);

        Passenger passenger2 = new Passenger();
        passenger2.setUsername("passenger2@noemail.com");
        entityManager.persist(passenger2);
        Ride ride3 = new Ride();
        ride3.setStatus(RideStatus.DRIVER_ARRIVING);
        entityManager.persist(ride3);
        PassengerRide passengerRide3 = new PassengerRide();
        passengerRide3.setPassenger(passenger2);
        passengerRide3.setRide(ride3);
        entityManager.persist(passengerRide3);

        entityManager.flush();

        List<String> usernames = new ArrayList<>();
        usernames.add(passenger1.getUsername());
        usernames.add(passenger2.getUsername());

        List<PassengerRide> passengerRides = passengerRideRepository
                .getCurrentPassengerRidesByUsername(usernames);
        assertFalse(passengerRides.isEmpty());
        assertEquals(passengerRides.size(), 2);
        assertTrue(passengerRides
                .stream()
                .anyMatch(pr -> pr.getPassenger() == passenger1));
        assertTrue(passengerRides
                .stream()
                .anyMatch(pr -> pr.getPassenger() == passenger2));
        assertFalse(passengerRides
                .stream()
                .anyMatch(pr -> pr.getRide().getStatus() == RideStatus.CANCELLED));
    }

    @Test
    void Return_empty_passenger_ride_list_when_no_passenger_has_a_current_ride() {
        Passenger passenger1 = new Passenger();
        passenger1.setUsername("passenger1@noemail.com");
        entityManager.persist(passenger1);
        Ride ride1 = new Ride();
        ride1.setStatus(RideStatus.COMPLETED);
        entityManager.persist(ride1);
        PassengerRide passengerRide1 = new PassengerRide();
        passengerRide1.setPassenger(passenger1);
        passengerRide1.setRide(ride1);
        entityManager.persist(passengerRide1);

        Passenger passenger2 = new Passenger();
        passenger2.setUsername("passenger2@noemail.com");
        entityManager.persist(passenger2);

        entityManager.flush();

        List<String> usernames = new ArrayList<>();
        usernames.add(passenger1.getUsername());
        usernames.add(passenger2.getUsername());

        List<PassengerRide> passengerRides = passengerRideRepository
                .getCurrentPassengerRidesByUsername(usernames);
        assertEquals(0, passengerRides.size());
    }


    //getClosestFreeDriver
    @Test
    void Return_free_driver_within_five_kilometers() {
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setRideActive(false);
        vehicle1.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle1.setCurrentCoordinates(new Coordinates(45.252782, 19.855517));
        vehicle1.setBabySeat(false);
        vehicle1.setPetsAllowed(false);
        entityManager.persist(vehicle1);
        Driver driver1 = new Driver();
        driver1.setActive(true);
        driver1.setVehicle(vehicle1);
        driver1.setUsername("driver1@noemail.com");
        entityManager.persist(driver1);

        Vehicle vehicle2 = new Vehicle();
        vehicle2.setRideActive(false);
        vehicle2.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle2.setCurrentCoordinates(new Coordinates(41.245782, 19.851122));
        vehicle2.setBabySeat(false);
        vehicle2.setPetsAllowed(false);
        entityManager.persist(vehicle2);
        Driver driver2 = new Driver();
        driver2.setActive(true);
        driver2.setVehicle(vehicle2);
        driver2.setUsername("driver2@noemail.com");
        entityManager.persist(driver2);

        Page<Driver> driverPage = driverRepository
                .getClosestFreeDriver(45.245749, 19.851122, false,
                        false, "COUPE", PageRequest.of(0, 1));

        assertEquals(1, driverPage.getTotalElements());
        assertEquals(driverPage.getContent().get(0), driver1);
    }

    @Test
    void Return_no_driver_if_none_are_within_five_kilometers() {
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setRideActive(false);
        vehicle1.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle1.setCurrentCoordinates(new Coordinates(43.252782, 19.855517));
        vehicle1.setBabySeat(false);
        vehicle1.setPetsAllowed(false);
        entityManager.persist(vehicle1);
        Driver driver1 = new Driver();
        driver1.setActive(true);
        driver1.setVehicle(vehicle1);
        driver1.setUsername("driver1@noemail.com");
        entityManager.persist(driver1);

        Vehicle vehicle2 = new Vehicle();
        vehicle2.setRideActive(false);
        vehicle2.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle2.setCurrentCoordinates(new Coordinates(41.245782, 19.851122));
        vehicle2.setBabySeat(false);
        vehicle2.setPetsAllowed(false);
        entityManager.persist(vehicle2);
        Driver driver2 = new Driver();
        driver2.setActive(true);
        driver2.setVehicle(vehicle2);
        driver2.setUsername("driver2@noemail.com");
        entityManager.persist(driver2);

        Page<Driver> driverPage = driverRepository
                .getClosestFreeDriver(45.245749, 19.851122, false,
                        false, "COUPE", PageRequest.of(0, 1));

        assertEquals(0, driverPage.getTotalElements());
    }

    @Test
    void Return_the_closer_driver() {
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setRideActive(false);
        vehicle1.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle1.setCurrentCoordinates(new Coordinates(45.252782, 19.855517));
        vehicle1.setBabySeat(false);
        vehicle1.setPetsAllowed(false);
        entityManager.persist(vehicle1);
        Driver driver1 = new Driver();
        driver1.setActive(true);
        driver1.setVehicle(vehicle1);
        driver1.setUsername("driver1@noemail.com");
        entityManager.persist(driver1);

        Vehicle vehicle2 = new Vehicle();
        vehicle2.setRideActive(false);
        vehicle2.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle2.setCurrentCoordinates(new Coordinates(45.245782, 19.851122));
        vehicle2.setBabySeat(false);
        vehicle2.setPetsAllowed(false);
        entityManager.persist(vehicle2);
        Driver driver2 = new Driver();
        driver2.setActive(true);
        driver2.setVehicle(vehicle2);
        driver2.setUsername("driver2@noemail.com");
        entityManager.persist(driver2);

        Page<Driver> driverPage = driverRepository
                .getClosestFreeDriver(45.245749, 19.851122, false,
                        false, "COUPE", PageRequest.of(0, 1));

        assertEquals(2, driverPage.getTotalElements());
        assertEquals(driverPage.getContent().get(0), driver2);
    }


    @Test
    void Return_no_driver_if_none_are_active() {
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setRideActive(false);
        vehicle1.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle1.setCurrentCoordinates(new Coordinates(45.252782, 19.855517));
        vehicle1.setBabySeat(false);
        vehicle1.setPetsAllowed(false);
        entityManager.persist(vehicle1);
        Driver driver1 = new Driver();
        driver1.setActive(false);
        driver1.setVehicle(vehicle1);
        driver1.setUsername("driver1@noemail.com");
        entityManager.persist(driver1);

        Vehicle vehicle2 = new Vehicle();
        vehicle2.setRideActive(false);
        vehicle2.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle2.setCurrentCoordinates(new Coordinates(45.245782, 19.851122));
        vehicle2.setBabySeat(false);
        vehicle2.setPetsAllowed(false);
        entityManager.persist(vehicle2);
        Driver driver2 = new Driver();
        driver2.setActive(false);
        driver2.setVehicle(vehicle2);
        driver2.setUsername("driver2@noemail.com");
        entityManager.persist(driver2);

        Page<Driver> driverPage = driverRepository
                .getClosestFreeDriver(45.245749, 19.851122, false,
                        false, "COUPE", PageRequest.of(0, 1));

        assertEquals(0, driverPage.getTotalElements());
    }

    @Test
    void Return_the_active_driver() {
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setRideActive(false);
        vehicle1.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle1.setCurrentCoordinates(new Coordinates(45.245782, 19.851122));
        vehicle1.setBabySeat(false);
        vehicle1.setPetsAllowed(false);
        entityManager.persist(vehicle1);
        Driver driver1 = new Driver();
        driver1.setActive(false);
        driver1.setVehicle(vehicle1);
        driver1.setUsername("driver1@noemail.com");
        entityManager.persist(driver1);

        Vehicle vehicle2 = new Vehicle();
        vehicle2.setRideActive(false);
        vehicle2.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle2.setCurrentCoordinates(new Coordinates(45.252782, 19.855517));
        vehicle2.setBabySeat(false);
        vehicle2.setPetsAllowed(false);
        entityManager.persist(vehicle2);
        Driver driver2 = new Driver();
        driver2.setActive(true);
        driver2.setVehicle(vehicle2);
        driver2.setUsername("driver2@noemail.com");
        entityManager.persist(driver2);

        Page<Driver> driverPage = driverRepository
                .getClosestFreeDriver(45.245749, 19.851122, false,
                        false, "COUPE", PageRequest.of(0, 1));

        assertEquals(1, driverPage.getTotalElements());
        assertEquals(driverPage.getContent().get(0), driver2);
    }

    @Test
    void Return_no_driver_if_all_have_an_active_ride() {
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setRideActive(true);
        vehicle1.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle1.setCurrentCoordinates(new Coordinates(45.245782, 19.851122));
        vehicle1.setBabySeat(false);
        vehicle1.setPetsAllowed(false);
        entityManager.persist(vehicle1);
        Ride ride1 = new Ride();
        entityManager.persist(ride1);
        Driver driver1 = new Driver();
        driver1.setCurrentRide(ride1);
        driver1.setActive(true);
        driver1.setVehicle(vehicle1);
        driver1.setUsername("driver1@noemail.com");
        entityManager.persist(driver1);

        Vehicle vehicle2 = new Vehicle();
        vehicle2.setRideActive(true);
        vehicle2.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle2.setCurrentCoordinates(new Coordinates(45.252782, 19.855517));
        vehicle2.setBabySeat(false);
        vehicle2.setPetsAllowed(false);
        entityManager.persist(vehicle2);
        Ride ride2 = new Ride();
        entityManager.persist(ride2);
        Driver driver2 = new Driver();
        driver2.setCurrentRide(ride2);
        driver2.setActive(true);
        driver2.setVehicle(vehicle2);
        driver2.setUsername("driver2@noemail.com");
        entityManager.persist(driver2);

        Page<Driver> driverPage = driverRepository
                .getClosestFreeDriver(45.245749, 19.851122, false,
                        false, "COUPE", PageRequest.of(0, 1));

        assertEquals(0, driverPage.getTotalElements());
    }

    @Test
    void Return_no_driver_if_none_meet_the_baby_seat_criteria() {
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setRideActive(false);
        vehicle1.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle1.setCurrentCoordinates(new Coordinates(45.245782, 19.851122));
        vehicle1.setBabySeat(false);
        vehicle1.setPetsAllowed(false);
        entityManager.persist(vehicle1);
        Driver driver1 = new Driver();
        driver1.setActive(true);
        driver1.setVehicle(vehicle1);
        driver1.setUsername("driver1@noemail.com");
        entityManager.persist(driver1);

        Vehicle vehicle2 = new Vehicle();
        vehicle2.setRideActive(false);
        vehicle2.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle2.setCurrentCoordinates(new Coordinates(45.252782, 19.855517));
        vehicle2.setBabySeat(false);
        vehicle2.setPetsAllowed(true);
        entityManager.persist(vehicle2);
        Driver driver2 = new Driver();
        driver2.setActive(true);
        driver2.setVehicle(vehicle2);
        driver2.setUsername("driver2@noemail.com");
        entityManager.persist(driver2);

        Page<Driver> driverPage = driverRepository
                .getClosestFreeDriver(45.245749, 19.851122, true,
                        false, "COUPE", PageRequest.of(0, 1));

        assertEquals(0, driverPage.getTotalElements());
    }

    @Test
    void Return_the_driver_that_meets_the_baby_seat_criteria() {
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setRideActive(false);
        vehicle1.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle1.setCurrentCoordinates(new Coordinates(45.245782, 19.851122));
        vehicle1.setBabySeat(false);
        vehicle1.setPetsAllowed(false);
        entityManager.persist(vehicle1);
        Driver driver1 = new Driver();
        driver1.setActive(true);
        driver1.setVehicle(vehicle1);
        driver1.setUsername("driver1@noemail.com");
        entityManager.persist(driver1);

        Vehicle vehicle2 = new Vehicle();
        vehicle2.setRideActive(false);
        vehicle2.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle2.setCurrentCoordinates(new Coordinates(45.252782, 19.855517));
        vehicle2.setBabySeat(true);
        vehicle2.setPetsAllowed(false);
        entityManager.persist(vehicle2);
        Driver driver2 = new Driver();
        driver2.setActive(true);
        driver2.setVehicle(vehicle2);
        driver2.setUsername("driver2@noemail.com");
        entityManager.persist(driver2);

        Page<Driver> driverPage = driverRepository
                .getClosestFreeDriver(45.245749, 19.851122, true,
                        false, "COUPE", PageRequest.of(0, 1));

        assertEquals(1, driverPage.getTotalElements());
        assertEquals(driverPage.getContent().get(0), driver2);
    }

    @Test
    void Return_no_driver_if_none_meet_the_pets_allowed_criteria() {
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setRideActive(false);
        vehicle1.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle1.setCurrentCoordinates(new Coordinates(45.245782, 19.851122));
        vehicle1.setBabySeat(false);
        vehicle1.setPetsAllowed(false);
        entityManager.persist(vehicle1);
        Driver driver1 = new Driver();
        driver1.setActive(true);
        driver1.setVehicle(vehicle1);
        driver1.setUsername("driver1@noemail.com");
        entityManager.persist(driver1);

        Vehicle vehicle2 = new Vehicle();
        vehicle2.setRideActive(false);
        vehicle2.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle2.setCurrentCoordinates(new Coordinates(45.252782, 19.855517));
        vehicle2.setBabySeat(true);
        vehicle2.setPetsAllowed(false);
        entityManager.persist(vehicle2);
        Driver driver2 = new Driver();
        driver2.setActive(true);
        driver2.setVehicle(vehicle2);
        driver2.setUsername("driver2@noemail.com");
        entityManager.persist(driver2);

        Page<Driver> driverPage = driverRepository
                .getClosestFreeDriver(45.245749, 19.851122, false,
                        true, "COUPE", PageRequest.of(0, 1));

        assertEquals(0, driverPage.getTotalElements());
    }

    @Test
    void Return_the_driver_that_meets_the_pets_allowed_criteria() {
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setRideActive(false);
        vehicle1.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle1.setCurrentCoordinates(new Coordinates(45.245782, 19.851122));
        vehicle1.setBabySeat(false);
        vehicle1.setPetsAllowed(false);
        entityManager.persist(vehicle1);
        Driver driver1 = new Driver();
        driver1.setActive(true);
        driver1.setVehicle(vehicle1);
        driver1.setUsername("driver1@noemail.com");
        entityManager.persist(driver1);

        Vehicle vehicle2 = new Vehicle();
        vehicle2.setRideActive(false);
        vehicle2.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle2.setCurrentCoordinates(new Coordinates(45.252782, 19.855517));
        vehicle2.setBabySeat(false);
        vehicle2.setPetsAllowed(true);
        entityManager.persist(vehicle2);
        Driver driver2 = new Driver();
        driver2.setActive(true);
        driver2.setVehicle(vehicle2);
        driver2.setUsername("driver2@noemail.com");
        entityManager.persist(driver2);

        Page<Driver> driverPage = driverRepository
                .getClosestFreeDriver(45.245749, 19.851122, false,
                        true, "COUPE", PageRequest.of(0, 1));

        assertEquals(1, driverPage.getTotalElements());
        assertEquals(driverPage.getContent().get(0), driver2);
    }

    @Test
    void Return_the_driver_that_meets_the_vehicle_type_criteria() {
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setRideActive(false);
        vehicle1.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle1.setCurrentCoordinates(new Coordinates(45.245782, 19.851122));
        vehicle1.setBabySeat(false);
        vehicle1.setPetsAllowed(false);
        entityManager.persist(vehicle1);
        Driver driver1 = new Driver();
        driver1.setActive(true);
        driver1.setVehicle(vehicle1);
        driver1.setUsername("driver1@noemail.com");
        entityManager.persist(driver1);

        Vehicle vehicle2 = new Vehicle();
        vehicle2.setRideActive(false);
        vehicle2.setVehicleType(vehicleTypeRepository.findByName("MINIVAN").get());
        vehicle2.setCurrentCoordinates(new Coordinates(45.252782, 19.855517));
        vehicle2.setBabySeat(false);
        vehicle2.setPetsAllowed(false);
        entityManager.persist(vehicle2);
        Driver driver2 = new Driver();
        driver2.setActive(true);
        driver2.setVehicle(vehicle2);
        driver2.setUsername("driver2@noemail.com");
        entityManager.persist(driver2);

        Page<Driver> driverPage = driverRepository
                .getClosestFreeDriver(45.245749, 19.851122, false,
                        false, "MINIVAN", PageRequest.of(0, 1));

        assertEquals(1, driverPage.getTotalElements());
        assertEquals(driverPage.getContent().get(0), driver2);
    }


    // getCloseBusyDriversWithNoNextRide
    @Test
    void Return_no_driver_if_none_are_busy() {
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setRideActive(false);
        vehicle1.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle1.setCurrentCoordinates(new Coordinates(45.252782, 19.855517));
        vehicle1.setNextCoordinates(new Coordinates(45.245749, 19.851122));
        vehicle1.setBabySeat(false);
        vehicle1.setPetsAllowed(false);
        entityManager.persist(vehicle1);
        Driver driver1 = new Driver();
        driver1.setActive(true);
        driver1.setVehicle(vehicle1);
        driver1.setUsername("driver1@noemail.com");
        entityManager.persist(driver1);

        Vehicle vehicle2 = new Vehicle();
        vehicle2.setRideActive(false);
        vehicle2.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle2.setCurrentCoordinates(new Coordinates(45.245782, 19.851122));
        vehicle2.setNextCoordinates(new Coordinates(45.245749, 19.851122));
        vehicle2.setBabySeat(false);
        vehicle2.setPetsAllowed(false);
        entityManager.persist(vehicle2);
        Driver driver2 = new Driver();
        driver2.setActive(true);
        driver2.setVehicle(vehicle2);
        driver2.setUsername("driver2@noemail.com");
        entityManager.persist(driver2);

        List<Driver> drivers = driverRepository
                .getCloseBusyDriversWithNoNextRide(45.245749, 19.851122, false,
                        false, "COUPE");

        assertEquals(0, drivers.size());
    }

    @Test
    void Return_the_busy_driver_within_five_kilometers() {
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setRideActive(true);
        vehicle1.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle1.setCurrentCoordinates(new Coordinates(45.252782, 19.855517));
        vehicle1.setNextCoordinates(new Coordinates(45.245749, 19.851122));
        vehicle1.setBabySeat(false);
        vehicle1.setPetsAllowed(false);
        entityManager.persist(vehicle1);
        Ride ride1 = new Ride();
        entityManager.persist(ride1);
        Driver driver1 = new Driver();
        driver1.setCurrentRide(ride1);
        driver1.setActive(true);
        driver1.setVehicle(vehicle1);
        driver1.setUsername("driver1@noemail.com");
        entityManager.persist(driver1);

        Vehicle vehicle2 = new Vehicle();
        vehicle2.setRideActive(true);
        vehicle2.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle2.setCurrentCoordinates(new Coordinates(41.245782, 19.851122));
        vehicle2.setNextCoordinates(new Coordinates(41.245749, 19.851122));
        vehicle2.setBabySeat(false);
        vehicle2.setPetsAllowed(false);
        entityManager.persist(vehicle2);
        Ride ride2 = new Ride();
        entityManager.persist(ride2);
        Driver driver2 = new Driver();
        driver2.setCurrentRide(ride2);
        driver2.setActive(true);
        driver2.setVehicle(vehicle2);
        driver2.setUsername("driver2@noemail.com");
        entityManager.persist(driver2);

        List<Driver> drivers = driverRepository
                .getCloseBusyDriversWithNoNextRide(45.245749, 19.851122, false,
                        false, "COUPE");

        assertEquals(1, drivers.size());
        assertEquals(drivers.get(0), driver1);
    }

    @Test
    void Return_no_busy_driver_if_none_are_within_five_kilometers() {
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setRideActive(true);
        vehicle1.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle1.setCurrentCoordinates(new Coordinates(42.252782, 19.855517));
        vehicle1.setNextCoordinates(new Coordinates(42.245749, 19.851122));
        vehicle1.setBabySeat(false);
        vehicle1.setPetsAllowed(false);
        entityManager.persist(vehicle1);
        Ride ride1 = new Ride();
        entityManager.persist(ride1);
        Driver driver1 = new Driver();
        driver1.setCurrentRide(ride1);
        driver1.setActive(true);
        driver1.setVehicle(vehicle1);
        driver1.setUsername("driver1@noemail.com");
        entityManager.persist(driver1);

        Vehicle vehicle2 = new Vehicle();
        vehicle2.setRideActive(true);
        vehicle2.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle2.setCurrentCoordinates(new Coordinates(43.245782, 19.851122));
        vehicle2.setNextCoordinates(new Coordinates(43.245749, 19.851122));
        vehicle2.setBabySeat(false);
        vehicle2.setPetsAllowed(false);
        entityManager.persist(vehicle2);
        Ride ride2 = new Ride();
        entityManager.persist(ride2);
        Driver driver2 = new Driver();
        driver2.setCurrentRide(ride2);
        driver2.setActive(true);
        driver2.setVehicle(vehicle2);
        driver2.setUsername("driver2@noemail.com");
        entityManager.persist(driver2);

        List<Driver> drivers = driverRepository
                .getCloseBusyDriversWithNoNextRide(45.245749, 19.851122, false,
                        false, "COUPE");

        assertEquals(0, drivers.size());
    }

    @Test
    void Return_no_busy_driver_if_all_have_a_next_ride_too() {
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setRideActive(true);
        vehicle1.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle1.setCurrentCoordinates(new Coordinates(45.252782, 19.855517));
        vehicle1.setNextCoordinates(new Coordinates(45.245749, 19.851122));
        vehicle1.setBabySeat(false);
        vehicle1.setPetsAllowed(false);
        entityManager.persist(vehicle1);
        Ride ride1 = new Ride();
        entityManager.persist(ride1);
        Ride ride11 = new Ride();
        entityManager.persist(ride11);
        Driver driver1 = new Driver();
        driver1.setCurrentRide(ride1);
        driver1.setNextRide(ride11);
        driver1.setActive(true);
        driver1.setVehicle(vehicle1);
        driver1.setUsername("driver1@noemail.com");
        entityManager.persist(driver1);

        Vehicle vehicle2 = new Vehicle();
        vehicle2.setRideActive(true);
        vehicle2.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle2.setCurrentCoordinates(new Coordinates(45.245782, 19.851122));
        vehicle2.setNextCoordinates(new Coordinates(45.245749, 19.851122));
        vehicle2.setBabySeat(false);
        vehicle2.setPetsAllowed(false);
        entityManager.persist(vehicle2);
        Ride ride2 = new Ride();
        entityManager.persist(ride2);
        Ride ride22 = new Ride();
        entityManager.persist(ride22);
        Driver driver2 = new Driver();
        driver1.setCurrentRide(ride2);
        driver1.setNextRide(ride22);
        driver2.setActive(true);
        driver2.setVehicle(vehicle2);
        driver2.setUsername("driver2@noemail.com");
        entityManager.persist(driver2);

        List<Driver> drivers = driverRepository
                .getCloseBusyDriversWithNoNextRide(45.245749, 19.851122, false,
                        false, "COUPE");

        assertEquals(0, drivers.size());
    }

    @Test
    void Return_no_busy_driver_if_none_meet_the_baby_seat_criteria() {
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setRideActive(false);
        vehicle1.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle1.setCurrentCoordinates(new Coordinates(45.245782, 19.851122));
        vehicle1.setNextCoordinates(new Coordinates(45.245749, 19.851122));
        vehicle1.setBabySeat(false);
        vehicle1.setPetsAllowed(false);
        entityManager.persist(vehicle1);
        Ride ride1 = new Ride();
        entityManager.persist(ride1);
        Driver driver1 = new Driver();
        driver1.setCurrentRide(ride1);
        driver1.setActive(true);
        driver1.setVehicle(vehicle1);
        driver1.setUsername("driver1@noemail.com");
        entityManager.persist(driver1);

        Vehicle vehicle2 = new Vehicle();
        vehicle2.setRideActive(false);
        vehicle2.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle2.setCurrentCoordinates(new Coordinates(45.252782, 19.855517));
        vehicle2.setNextCoordinates(new Coordinates(45.245749, 19.851122));
        vehicle2.setBabySeat(false);
        vehicle2.setPetsAllowed(true);
        entityManager.persist(vehicle2);
        Ride ride2 = new Ride();
        entityManager.persist(ride2);
        Driver driver2 = new Driver();
        driver1.setCurrentRide(ride2);
        driver2.setActive(true);
        driver2.setVehicle(vehicle2);
        driver2.setUsername("driver2@noemail.com");
        entityManager.persist(driver2);

        List<Driver> drivers = driverRepository
                .getCloseBusyDriversWithNoNextRide(45.245749, 19.851122, true,
                        false, "COUPE");

        assertEquals(0, drivers.size());
    }

    @Test
    void Return_the_busy_driver_that_meets_the_baby_seat_criteria() {
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setRideActive(true);
        vehicle1.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle1.setCurrentCoordinates(new Coordinates(45.245782, 19.851122));
        vehicle1.setNextCoordinates(new Coordinates(45.245749, 19.851122));
        vehicle1.setBabySeat(false);
        vehicle1.setPetsAllowed(false);
        entityManager.persist(vehicle1);
        Ride ride1 = new Ride();
        entityManager.persist(ride1);
        Driver driver1 = new Driver();
        driver1.setCurrentRide(ride1);
        driver1.setActive(true);
        driver1.setVehicle(vehicle1);
        driver1.setUsername("driver1@noemail.com");
        entityManager.persist(driver1);

        Vehicle vehicle2 = new Vehicle();
        vehicle2.setRideActive(true);
        vehicle2.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle2.setCurrentCoordinates(new Coordinates(45.252782, 19.855517));
        vehicle2.setNextCoordinates(new Coordinates(45.245749, 19.851122));
        vehicle2.setBabySeat(true);
        vehicle2.setPetsAllowed(false);
        entityManager.persist(vehicle2);
        Ride ride2 = new Ride();
        entityManager.persist(ride2);
        Driver driver2 = new Driver();
        driver2.setCurrentRide(ride2);
        driver2.setActive(true);
        driver2.setVehicle(vehicle2);
        driver2.setUsername("driver2@noemail.com");
        entityManager.persist(driver2);

        List<Driver> drivers = driverRepository
                .getCloseBusyDriversWithNoNextRide(45.245749, 19.851122, true,
                        false, "COUPE");

        assertEquals(1, drivers.size());
        assertEquals(drivers.get(0), driver2);
    }

    @Test
    void Return_no_busy_driver_if_none_meet_the_pets_allowed_criteria() {
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setRideActive(true);
        vehicle1.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle1.setCurrentCoordinates(new Coordinates(45.245782, 19.851122));
        vehicle1.setNextCoordinates(new Coordinates(45.245749, 19.851122));
        vehicle1.setBabySeat(false);
        vehicle1.setPetsAllowed(false);
        entityManager.persist(vehicle1);
        Ride ride1 = new Ride();
        entityManager.persist(ride1);
        Driver driver1 = new Driver();
        driver1.setCurrentRide(ride1);
        driver1.setActive(true);
        driver1.setVehicle(vehicle1);
        driver1.setUsername("driver1@noemail.com");
        entityManager.persist(driver1);

        Vehicle vehicle2 = new Vehicle();
        vehicle2.setRideActive(true);
        vehicle2.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle2.setCurrentCoordinates(new Coordinates(45.252782, 19.855517));
        vehicle2.setNextCoordinates(new Coordinates(45.245749, 19.851122));
        vehicle2.setBabySeat(true);
        vehicle2.setPetsAllowed(false);
        entityManager.persist(vehicle2);
        Ride ride2 = new Ride();
        entityManager.persist(ride2);
        Driver driver2 = new Driver();
        driver1.setCurrentRide(ride2);
        driver2.setActive(true);
        driver2.setVehicle(vehicle2);
        driver2.setUsername("driver2@noemail.com");
        entityManager.persist(driver2);

        List<Driver> drivers = driverRepository
                .getCloseBusyDriversWithNoNextRide(45.245749, 19.851122, false,
                        true, "COUPE");

        assertEquals(0, drivers.size());
    }

    @Test
    void Return_the_busy_driver_that_meets_the_pets_allowed_criteria() {
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setRideActive(true);
        vehicle1.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle1.setCurrentCoordinates(new Coordinates(45.245782, 19.851122));
        vehicle1.setNextCoordinates(new Coordinates(45.245749, 19.851122));
        vehicle1.setBabySeat(false);
        vehicle1.setPetsAllowed(false);
        entityManager.persist(vehicle1);
        Ride ride1 = new Ride();
        entityManager.persist(ride1);
        Driver driver1 = new Driver();
        driver1.setCurrentRide(ride1);
        driver1.setActive(true);
        driver1.setVehicle(vehicle1);
        driver1.setUsername("driver1@noemail.com");
        entityManager.persist(driver1);

        Vehicle vehicle2 = new Vehicle();
        vehicle2.setRideActive(true);
        vehicle2.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle2.setCurrentCoordinates(new Coordinates(45.252782, 19.855517));
        vehicle2.setNextCoordinates(new Coordinates(45.245749, 19.851122));
        vehicle2.setBabySeat(false);
        vehicle2.setPetsAllowed(true);
        entityManager.persist(vehicle2);
        Ride ride2 = new Ride();
        entityManager.persist(ride2);
        Driver driver2 = new Driver();
        driver2.setCurrentRide(ride2);
        driver2.setActive(true);
        driver2.setVehicle(vehicle2);
        driver2.setUsername("driver2@noemail.com");
        entityManager.persist(driver2);

        List<Driver> drivers = driverRepository
                .getCloseBusyDriversWithNoNextRide(45.245749, 19.851122, false,
                        true, "COUPE");

        assertEquals(1, drivers.size());
        assertEquals(drivers.get(0), driver2);
    }

    @Test
    void Return_the_busy_driver_that_meets_the_vehicle_type_criteria() {
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setRideActive(true);
        vehicle1.setVehicleType(vehicleTypeRepository.findByName("COUPE").get());
        vehicle1.setCurrentCoordinates(new Coordinates(45.245782, 19.851122));
        vehicle1.setNextCoordinates(new Coordinates(45.245749, 19.851122));
        vehicle1.setBabySeat(false);
        vehicle1.setPetsAllowed(false);
        entityManager.persist(vehicle1);
        Ride ride1 = new Ride();
        entityManager.persist(ride1);
        Driver driver1 = new Driver();
        driver1.setCurrentRide(ride1);
        driver1.setActive(true);
        driver1.setVehicle(vehicle1);
        driver1.setUsername("driver1@noemail.com");
        entityManager.persist(driver1);

        Vehicle vehicle2 = new Vehicle();
        vehicle2.setRideActive(true);
        vehicle2.setVehicleType(vehicleTypeRepository.findByName("MINIVAN").get());
        vehicle2.setCurrentCoordinates(new Coordinates(45.252782, 19.855517));
        vehicle2.setNextCoordinates(new Coordinates(45.245749, 19.851122));
        vehicle2.setBabySeat(false);
        vehicle2.setPetsAllowed(false);
        entityManager.persist(vehicle2);
        Ride ride2 = new Ride();
        entityManager.persist(ride2);
        Driver driver2 = new Driver();
        driver2.setCurrentRide(ride2);
        driver2.setActive(true);
        driver2.setVehicle(vehicle2);
        driver2.setUsername("driver2@noemail.com");
        entityManager.persist(driver2);

        List<Driver> drivers = driverRepository
                .getCloseBusyDriversWithNoNextRide(45.245749, 19.851122, false,
                        false, "MINIVAN");

        assertEquals(1, drivers.size());
        assertEquals(drivers.get(0), driver2);
    }

}
