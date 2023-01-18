package com.example.springbackend.dto.display;

import lombok.Data;

import java.util.*;

@Data
public class DetailedRideHistoryPassengerDTO {
    private DriverDisplayDTO driver;
    private Map<String, Double> driverRating = new HashMap<String, Double>() {};
    private Map<String, Double> vehicleRating = new HashMap<String, Double>() {};
}
