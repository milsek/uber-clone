package com.example.springbackend.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
@Data
public class Driver extends Member{

    private Boolean active;

    @OneToOne
    Vehicle vehicle;
}
