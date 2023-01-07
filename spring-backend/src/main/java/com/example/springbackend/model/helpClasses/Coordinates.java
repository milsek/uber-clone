package com.example.springbackend.model.helpClasses;

import javax.persistence.Embeddable;

@Embeddable
public class Coordinates {
    private Double lat;
    private Double lng;
    public Coordinates(Double lat, Double lng){
        this.lat = lat;
        this.lng = lng;
    }

    public Coordinates() {

    }

    public Double getLat(){ return lat; }
    public Double getLng(){ return lng; }
    public void setLat(Double lat){ this.lat = lat; }
    public void setLng(Double lng){ this.lng = lng; }
}
