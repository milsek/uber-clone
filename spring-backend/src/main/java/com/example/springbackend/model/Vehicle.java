package com.example.springbackend.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    private boolean babySeat;

    private boolean petsAllowed;

    private String make;

    private String model;

    private String colour;

    private String licensePlateNumber;

    @ManyToOne
    @JoinColumn
    private VehicleType vehicleType;
}
