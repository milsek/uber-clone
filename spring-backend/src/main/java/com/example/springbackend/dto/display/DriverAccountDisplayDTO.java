package com.example.springbackend.dto.display;

import lombok.Data;

@Data
public class DriverAccountDisplayDTO extends AccountDisplayDTO {
    private double distanceTravelled;
    private int ridesCompleted;
    private int totalRatingSum;
    private int numberOfReviews;
    private VehicleDisplayDTO vehicle;
    private boolean active;
}
