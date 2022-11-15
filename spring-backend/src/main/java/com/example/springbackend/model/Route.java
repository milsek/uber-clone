package com.example.springbackend.model;

import com.example.springbackend.model.helpClasses.Coordinates;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ElementCollection
    private List<Coordinates<Double,Double>> coordinates;

    @ElementCollection
    private List<Coordinates<Double,Double>> waypoints;

}
