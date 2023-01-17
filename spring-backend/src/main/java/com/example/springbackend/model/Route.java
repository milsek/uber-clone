package com.example.springbackend.model;

import com.example.springbackend.model.helpClasses.Coordinates;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ElementCollection
    private List<Coordinates> coordinates;

    @ElementCollection
    private List<Coordinates> waypoints;

    @ManyToMany(mappedBy = "favouriteRoutes")
    @JsonBackReference
    List<Passenger> passengers;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<Coordinates> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Coordinates> coordinates) {
        this.coordinates = coordinates;
    }

    public List<Coordinates> getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(List<Coordinates> waypoints) {
        this.waypoints = waypoints;
    }

    public List<Passenger> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<Passenger> passengers) {
        this.passengers = passengers;
    }

    @Override
    public String toString(){
        return this.id.toString();
    }
}
