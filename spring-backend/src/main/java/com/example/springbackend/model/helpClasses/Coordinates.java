package com.example.springbackend.model.helpClasses;

import javax.persistence.Embeddable;

@Embeddable
public class Coordinates<Lat,Lng> {
    private Lat lat;
    private Lng lng;
    public Coordinates(Lat lat, Lng lng){
        this.lat = lat;
        this.lng = lng;
    }

    public Coordinates() {

    }

    public Lat getLat(){ return lat; }
    public Lng getLng(){ return lng; }
    public void setLat(Lat lat){ this.lat = lat; }
    public void setLng(Lng lng){ this.lng = lng; }
}
