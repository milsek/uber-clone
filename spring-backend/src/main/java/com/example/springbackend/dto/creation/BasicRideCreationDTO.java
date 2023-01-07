package com.example.springbackend.dto.creation;

import lombok.Data;

import javax.validation.constraints.*;

@Data
public class BasicRideCreationDTO {
    @DecimalMin(value = "0.25", message = "Minimum ride distance is 0.25km.")
    @Max(value = 100, message = "Maximum ride distance is 100km.")
    private Double distance;
    @Min(1)
    private int expectedTime;
    private boolean babySeat;
    private boolean petFriendly;
    @NotBlank
    private String vehicleType;
    @NotNull
    private RouteCreationDTO actualRoute;
    private RouteCreationDTO expectedRoute;
}
