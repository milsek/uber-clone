package com.example.springbackend.repository;

import com.example.springbackend.model.Admin;
import com.example.springbackend.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, String> {
    Optional<Driver> findByUsername(String username);
}