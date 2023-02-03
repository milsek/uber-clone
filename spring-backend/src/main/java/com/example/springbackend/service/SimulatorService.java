package com.example.springbackend.service;

import com.example.springbackend.model.Driver;
import com.example.springbackend.model.Vehicle;
import com.example.springbackend.model.helpClasses.Coordinates;
import com.example.springbackend.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class SimulatorService {
    @Autowired
    TestDataSupplierService testDataSupplierService;

    @Autowired
    VehicleRepository vehicleRepository;

    private static final double r2d = 180.0D / 3.141592653589793D;
    private static final double d2r = 3.141592653589793D / 180.0D;
    private static final double d2km = 111189.57696D * r2d;

    public void simulateMove(Authentication auth) {
        Driver driver = (Driver) auth.getPrincipal();
        if (!driver.getActive()) return;

        Vehicle vehicle = driver.getVehicle();
        if (vehicle.isRideActive()) return;

        Coordinates location = getDifferentLocation(vehicle);

        vehicle.setNextCoordinates(location);
        vehicle.setCoordinatesChangedAt(LocalDateTime.now());
        vehicle.setRideActive(true);

        long estimatedTime = getEstimatedTime(vehicle);
        vehicle.setExpectedTripTime(estimatedTime);

        vehicleRepository.save(vehicle);

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.schedule(() -> arriveAtLocation(vehicle, false), estimatedTime, TimeUnit.SECONDS);
    }

    public long getEstimatedTime(Vehicle vehicle) {
        double aerialDistance = calculateDistance(vehicle.getCurrentCoordinates(), vehicle.getNextCoordinates());
        long estimatedTime = calculateTimeSeconds(aerialDistance);
        return estimatedTime;
    }

    private Coordinates getDifferentLocation(Vehicle vehicle) {
        Coordinates location = vehicle.getCurrentCoordinates();
        Random rand = new Random();
        while (Math.abs(location.getLat() - vehicle.getCurrentCoordinates().getLat()) < Double.MIN_NORMAL &&
                Math.abs(location.getLng() - vehicle.getCurrentCoordinates().getLng()) < Double.MIN_NORMAL) {
            location = TestDataSupplierService.locations.get(rand.nextInt(0,
                    TestDataSupplierService.locations.size()));
        }
        return location;
    }

    public void arriveAtLocation(Vehicle vehicle, boolean setRideActive) {
        vehicle.setCurrentCoordinates(vehicle.getNextCoordinates());
        vehicle.setExpectedTripTime(0);
        vehicle.setRideActive(setRideActive);
        vehicleRepository.save(vehicle);
    }

    private double calculateDistance(Coordinates first, Coordinates second) {
        double lt1 = first.getLat();
        double lt2 = second.getLat();
        double ln1 = first.getLng();
        double ln2 = second.getLng();
        double x = lt1 * d2r;
        double y = lt2 * d2r;
        return Math.acos( Math.sin(x) * Math.sin(y) + Math.cos(x) * Math.cos(y) * Math.cos(d2r * (ln1 - ln2))) * d2km;
    }

    private long calculateTimeSeconds(double aerialDistance) {
        // a rough estimate
        double drivingDistance = 1.25 * aerialDistance;
        return Math.round(((drivingDistance / 1000) * 2) * 60);
    }
}
