package com.example.springbackend.model;

import lombok.Data;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
public class Passenger extends Member {

    private String paymentDetails;

    @ManyToMany()
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinTable(name = "favourite_routes",
            joinColumns = @JoinColumn(name = "passanger_username", referencedColumnName = "username"),
            inverseJoinColumns = @JoinColumn(name = "route_id", referencedColumnName = "id"))
    private List<Route> favouriteRoutes;
}
