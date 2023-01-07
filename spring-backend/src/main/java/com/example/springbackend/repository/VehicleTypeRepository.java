package com.example.springbackend.repository;

import com.example.springbackend.model.Admin;
import com.example.springbackend.model.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleTypeRepository extends JpaRepository<VehicleType, Integer> {
    @Override
    Optional<VehicleType> findById(Integer integer);

    Optional<VehicleType> findByName(String name);
}
