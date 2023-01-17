package com.example.springbackend.dto.display;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DetailedRideHistoryDriverDTO {
    private List<PassengerDisplayDTO> passengers;

    public DetailedRideHistoryDriverDTO() {
        this.passengers = new ArrayList<PassengerDisplayDTO>();
    }

    public void addPassenger(PassengerDisplayDTO map) {
        passengers.add(map);
    }
}
