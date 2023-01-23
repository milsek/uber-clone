package com.example.springbackend.dto.display;

import com.example.springbackend.model.Driver;
import lombok.Data;

import java.util.List;

@Data
public class DriverSearchDisplayDTO {
    private List<Driver> drivers;
    private Integer numberOfDrivers;
}
