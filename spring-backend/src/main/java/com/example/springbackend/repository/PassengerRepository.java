package com.example.springbackend.repository;

import com.example.springbackend.model.Admin;
import com.example.springbackend.model.Passenger;
import com.example.springbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, String> {

    Optional<Passenger> findByUsername(String username);
    Optional<Passenger> findByEmail(String username);
}
