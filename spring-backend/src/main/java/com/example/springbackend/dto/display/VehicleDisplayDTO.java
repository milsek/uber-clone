package com.example.springbackend.dto.display;

import lombok.Data;

@Data
public class VehicleDisplayDTO {
    boolean babySeat;
    boolean petsAllowed;
    String make;
    String model;
    String colour;
    String licensePlateNumber;
    VehicleTypeDisplayDTO vehicleType;
}
