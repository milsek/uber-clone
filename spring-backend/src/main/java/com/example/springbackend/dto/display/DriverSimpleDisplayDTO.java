package com.example.springbackend.dto.display;

import lombok.Data;

@Data
public class DriverSimpleDisplayDTO {
    private String username;
    private String name;
    private String surname;
    private String phoneNumber;
    private String profilePicture;
    private int totalRatingSum;
    private int numberOfReviews;
    private VehicleDisplayDTO vehicle;
}
