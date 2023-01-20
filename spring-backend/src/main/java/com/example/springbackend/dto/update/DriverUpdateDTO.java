package com.example.springbackend.dto.update;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DriverUpdateDTO extends UserUpdateDTO {
    private Boolean babySeat;

    private Boolean petsAllowed;

    private String make;

    private String model;

    private String colour;

    private String licensePlateNumber;

    private String vehicleType;
}
