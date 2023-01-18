package com.example.springbackend.dto.creation;

import lombok.Data;

@Data
public class ReviewDTO {
    private Integer rideId;
    private int driverRating;
    private int vehicleRating;
    private String comment;
}
