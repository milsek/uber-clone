package com.example.springbackend.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }) })
public class VehicleType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private double price;

    private int seats;
}
