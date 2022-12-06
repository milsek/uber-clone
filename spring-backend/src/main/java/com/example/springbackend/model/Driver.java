package com.example.springbackend.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
@Data
public class Driver extends Member{

    private Boolean active;

    private double distanceTravelled;

    private int ridesCompleted;

    private int totalRatingSum;

    private int numberOfReviews;

    @OneToOne
    Vehicle vehicle;
}