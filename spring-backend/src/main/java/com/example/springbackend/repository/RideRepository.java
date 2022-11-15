package com.example.springbackend.repository;

import com.example.springbackend.model.Admin;
import com.example.springbackend.model.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RideRepository extends JpaRepository<Ride, Integer> {
}
