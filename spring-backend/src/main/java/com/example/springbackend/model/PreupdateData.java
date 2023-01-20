package com.example.springbackend.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name="preupdateData")
public class PreupdateData {
    @Id
    private String username;
    private String name;
    private String surname;
    private String phoneNumber;
    private String city;
    private String profilePicture;
    private Boolean babySeat;
    private Boolean petsAllowed;
    private String make;
    private String model;
    private String colour;
    private String licensePlateNumber;
    private String vehicleType;
}
