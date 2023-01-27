package com.example.springbackend.dto.display;

import lombok.Data;

import java.util.*;

@Data
public class DetailedRideHistoryPassengerDTO {
    private DriverDisplayDTO driver;
    private Map<String, Integer> driverRating = new HashMap<String, Integer>() {};
    private Map<String, Integer> vehicleRating = new HashMap<String, Integer>() {};
}
