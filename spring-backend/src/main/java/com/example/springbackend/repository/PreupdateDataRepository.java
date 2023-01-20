package com.example.springbackend.repository;

import com.example.springbackend.model.PreupdateData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PreupdateDataRepository extends JpaRepository<PreupdateData, String> {
    Optional<PreupdateData> findByUsername(String username);
}
