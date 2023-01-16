package com.example.springbackend.dto.creation;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class DriverCreationDTO extends UserCreationDTO {
    private Boolean babySeat;

    private Boolean petsAllowed;

    @NotBlank
    private String make;

    @NotBlank
    private String model;

    @NotBlank
    private String colour;

    @NotBlank
    private String licensePlateNumber;

    @NotBlank
    private String vehicleType;
}
