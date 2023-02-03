package com.example.springbackend.dto.update;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
public class DriverUpdateDTO extends UserUpdateDTO {
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
