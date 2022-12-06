package com.example.springbackend.model;

import com.example.springbackend.model.helpClasses.AuthenticationProvider;
import lombok.Data;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
public class Passenger extends Member {

    private String paymentDetails;

    private double distanceTravelled;

    private int ridesCompleted;

    @ManyToMany()
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinTable(name = "favourite_routes",
            joinColumns = @JoinColumn(name = "passenger_username", referencedColumnName = "username"),
            inverseJoinColumns = @JoinColumn(name = "route_id", referencedColumnName = "id"))
    private List<Route> favouriteRoutes;

    public String getPaymentDetails() {
        return paymentDetails;
    }

    public void setPaymentDetails(String paymentDetails) {
        this.paymentDetails = paymentDetails;
    }
}
