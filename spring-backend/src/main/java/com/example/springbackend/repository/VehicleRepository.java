package com.example.springbackend.repository;

import com.example.springbackend.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {
    @Query(value="SELECT v FROM Vehicle v JOIN Driver d ON d.vehicle = v WHERE d.active = true")
    Collection<Vehicle> findVehiclesWhoseDriversAreActive();
}
