package com.example.springbackend.service;

import com.example.springbackend.model.Role;
import com.example.springbackend.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {

    @Autowired
    RoleRepository roleRepository;

    public List<Role> findByName(String name) {
        return this.roleRepository.findByName(name);
    }
}
