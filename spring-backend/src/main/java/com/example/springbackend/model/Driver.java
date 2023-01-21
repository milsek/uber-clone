package com.example.springbackend.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Driver extends Member{

    private Boolean active;

    private double distanceTravelled;

    private int ridesCompleted;

    private int totalRatingSum;

    private int numberOfReviews;

    double activeMinutesToday;
    LocalDateTime lastSetActive;
    @OneToOne
    Vehicle vehicle;

    @OneToOne
    Ride currentRide;

    @OneToOne
    Ride nextRide;
}
