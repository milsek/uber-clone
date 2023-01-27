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
    private String driverRejectionReason;
    private RideStatus status;
    private String startAddress;
    private String destinationAddress;
    private Boolean passengersConfirmed;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;
    private Boolean driverInconsistencyReported;
    private int price;
    private String vehicleType;
    private boolean babySeatRequested;
    private boolean petFriendlyRequested;
    private int delayInMinutes;

    @ManyToOne
    @JoinColumn
    private Driver driver;

    @ManyToOne
    @JoinColumn
    private Route route;
}
