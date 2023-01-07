package com.example.springbackend.model;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;
@Entity
@Where(clause = "banned = false")
public class Passenger extends Member {

    private String paymentDetails;

    private double distanceTravelled;

    private int ridesCompleted;

    @PositiveOrZero
    private int tokenBalance;

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

    public double getDistanceTravelled() {
        return distanceTravelled;
    }

    public void setDistanceTravelled(double distanceTravelled) {
        this.distanceTravelled = distanceTravelled;
    }

    public int getRidesCompleted() {
        return ridesCompleted;
    }

    public void setRidesCompleted(int ridesCompleted) {
        this.ridesCompleted = ridesCompleted;
    }

    public List<Route> getFavouriteRoutes() {
        return favouriteRoutes;
    }

    public void setFavouriteRoutes(List<Route> favouriteRoutes) {
        this.favouriteRoutes = favouriteRoutes;
    }

    public int getTokenBalance() {
        return tokenBalance;
    }

    public void setTokenBalance(int tokenBalance) {
        this.tokenBalance = tokenBalance;
    }
}
