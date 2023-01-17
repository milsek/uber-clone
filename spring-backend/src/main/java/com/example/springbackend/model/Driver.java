package com.example.springbackend.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
@Getter
@Setter
public class Driver extends Member{

    private Boolean active;

    private double distanceTravelled;

    private int ridesCompleted;

    private int totalRatingSum;

    private int numberOfReviews;

    @OneToOne
    Vehicle vehicle;

    @OneToOne
    Ride currentRide;

    @OneToOne
    Ride nextRide;
}
