package com.example.springbackend.dto.display;

import lombok.Data;

@Data
public class DriverCurrentAndNextRideDisplayDTO {
    private DriverRideDisplayDTO currentRide;
    private DriverRideDisplayDTO nextRide;
}
