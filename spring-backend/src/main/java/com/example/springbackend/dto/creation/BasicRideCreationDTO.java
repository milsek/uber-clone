package com.example.springbackend.dto.creation;

import lombok.Data;
import org.springframework.lang.Nullable;

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
    private String startAddress;
    @NotBlank
    private String destinationAddress;
    @NotBlank
    private String vehicleType;
    @NotNull
    private RouteCreationDTO actualRoute;
    private RouteCreationDTO expectedRoute;
    @Min(value = 0, message="Delay in minutes must be a non-negative integer.")
    @Max(value = 300, message="Delay in minutes cannot be greater than 300.")
    private int delayInMinutes;
}
