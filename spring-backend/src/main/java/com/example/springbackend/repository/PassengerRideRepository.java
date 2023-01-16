package com.example.springbackend.repository;

import com.example.springbackend.model.Passenger;
import com.example.springbackend.model.PassengerRide;
import com.example.springbackend.model.Ride;
import io.swagger.models.auth.In;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface PassengerRideRepository extends JpaRepository<PassengerRide, Integer> {
    @Query("SELECT pr.ride FROM PassengerRide pr WHERE " +
            "pr.passenger = :passenger AND pr.ride.rejected = false " +
            "AND pr.ride.endTime is null")
    Optional<Ride> getCurrentRide(@Param("passenger") Passenger passenger);

    Optional<PassengerRide> findByRideAndPassengerUsername(Ride ride, String username);

    @Query("SELECT pr.passenger.username FROM PassengerRide pr WHERE " +
            "pr.ride.id = :rideId")
    List<String> getPassengersForRide(@Param("rideId") Integer rideId);
}

