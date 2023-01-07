package com.example.springbackend.repository;

import com.example.springbackend.model.Driver;
import com.example.springbackend.model.Ride;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, String> {
    static final String HAVERSINE_PART = "(6371 * (2 * asin(sqrt(sin(radians(d.vehicle.currentCoordinates.lat - :lat) / 2) * sin(radians(d.vehicle.currentCoordinates.lat - :lat) / 2) + sin(radians(d.vehicle.currentCoordinates.lng - :lng) / 2) * sin(radians(d.vehicle.currentCoordinates.lng - :lng) / 2) * cos(:lat) * cos(d.vehicle.currentCoordinates.lat)))))";
    static final String HAVERSINE_PART_NEXT = "(6371 * (2 * asin(sqrt(sin(radians(d.vehicle.nextCoordinates.lat - :lat) / 2) * sin(radians(d.vehicle.nextCoordinates.lat - :lat) / 2) + sin(radians(d.vehicle.nextCoordinates.lng - :lng) / 2) * sin(radians(d.vehicle.nextCoordinates.lng - :lng) / 2) * cos(:lat) * cos(d.vehicle.nextCoordinates.lat)))))";

    Optional<Driver> findByUsername(String username);

    @Query("SELECT d FROM Driver d WHERE d.active = true AND d.currentRide is null AND d.nextRide is null " +
            "AND d.vehicle.rideActive = false AND d.vehicle.babySeat >= :babySeat AND " +
            "d.vehicle.petsAllowed >= :petFriendly AND d.vehicle.vehicleType.name = :vehicleType AND " +
            "" + HAVERSINE_PART + " <= 5 ORDER BY " + HAVERSINE_PART + " ASC")
    Page<Driver> getClosestFreeDriver(
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("babySeat") boolean babySeat,
            @Param("petFriendly") boolean petFriendly,
            @Param("vehicleType") String vehicleType,
            Pageable page);

    @Query("SELECT d FROM Driver d WHERE d.active = true AND d.currentRide is not null AND d.nextRide is null " +
            "AND d.vehicle.rideActive = true AND d.vehicle.babySeat >= :babySeat AND " +
            "d.vehicle.petsAllowed >= :petFriendly AND d.vehicle.vehicleType.name = :vehicleType AND " +
            "" + HAVERSINE_PART_NEXT + " <= 5")
    List<Driver> getCloseBusyDriversWithNoNextRide(
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("babySeat") boolean babySeat,
            @Param("petFriendly") boolean petFriendly,
            @Param("vehicleType") String vehicleType
    );

    @Query("SELECT d FROM Driver d WHERE d.currentRide = :ride OR d.nextRide = :ride")
    Optional<Driver> getDriverForRide( @Param("ride") Ride currentRide);
}
