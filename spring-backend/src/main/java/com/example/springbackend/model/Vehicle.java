package com.example.springbackend.model;

import com.example.springbackend.model.helpClasses.Coordinates;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    private Boolean babySeat;

    private Boolean petsAllowed;

    private String make;

    private String model;

    private String colour;

    private String licensePlateNumber;

    private boolean rideActive;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride( name = "lat", column = @Column(name = "current_lat")),
            @AttributeOverride( name = "lng", column = @Column(name = "current_lng")),
    })
    private Coordinates currentCoordinates;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride( name = "lat", column = @Column(name = "next_lat")),
            @AttributeOverride( name = "lng", column = @Column(name = "next_lng")),
    })
    private Coordinates nextCoordinates;

    private LocalDateTime coordinatesChangedAt;

    private long expectedTripTime;

    @ManyToOne
    @JoinColumn
    private VehicleType vehicleType;
}
