package com.example.springbackend.dto.display;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DetailedRideHistoryPassengerDTO {
    private DriverDisplayDTO driver;
    private Map<String, Double> driverRating;
    private Map<String, Double> vehicleRating;
}
