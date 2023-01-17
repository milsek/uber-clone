package com.example.springbackend.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Double distance;
    private int expectedTime;
    private String driverCancelled;
    private Boolean rejected;
    private Boolean passengersConfirmed;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;
    private Boolean driverInconsistency;
    private int price;
    private String vehicleType;

    @ManyToOne
    @JoinColumn
    private Driver driver;

    @ManyToOne
    @JoinColumn
    private Route actualRoute;

    @ManyToOne
    @JoinColumn
    private Route expectedRoute;
}
