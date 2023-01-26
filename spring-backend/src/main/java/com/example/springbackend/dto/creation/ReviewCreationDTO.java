package com.example.springbackend.dto.creation;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

@Data
public class ReviewCreationDTO {
    private Integer rideId;
    @Min(1)
    @Max(5)
    private int driverRating;
    @Min(1)
    @Max(5)
    private int vehicleRating;
    @Size(min = 1, max = 255)
    private String comment;
}
