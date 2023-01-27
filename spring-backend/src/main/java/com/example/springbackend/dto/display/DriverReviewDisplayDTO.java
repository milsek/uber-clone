package com.example.springbackend.dto.display;

import lombok.Data;

@Data
public class DriverReviewDisplayDTO {
    private int driverRating;
    private int vehicleRating;
    private String comment;
    private PassengerDisplayDTO passenger;
}
