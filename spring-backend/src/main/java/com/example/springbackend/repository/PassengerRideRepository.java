package com.example.springbackend.repository;

import com.example.springbackend.model.Passenger;
import com.example.springbackend.model.PassengerRide;
import com.example.springbackend.model.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PassengerRideRepository extends JpaRepository<PassengerRide, Integer> {
    @Query("SELECT pr.ride FROM PassengerRide pr WHERE " +
            "pr.passenger = :passenger AND pr.ride.rejected = false " +
            "AND pr.ride.endTime is null")
    Optional<Ride> getCurrentRide(@Param("passenger") Passenger passenger);
}

