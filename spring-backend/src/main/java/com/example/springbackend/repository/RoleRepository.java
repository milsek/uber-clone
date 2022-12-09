package com.example.springbackend.repository;

import com.example.springbackend.model.Role;
import com.example.springbackend.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    List<Role> findByName(String name);
}
