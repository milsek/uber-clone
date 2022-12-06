package com.example.springbackend.dto.display;

import lombok.Data;

@Data
public class DriverDisplayDTO {
    private String username;
    private String name;
    private String surname;
    private String phoneNumber;
    private String city;
    private String profilePicture;
    private double distanceTravelled;
    private int ridesCompleted;
    private int totalRatingSum;
    private int numberOfReviews;
    private VehicleDisplayDTO vehicle;
    private boolean active;
}
