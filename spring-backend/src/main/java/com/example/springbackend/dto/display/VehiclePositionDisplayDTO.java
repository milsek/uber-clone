package com.example.springbackend.dto.display;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VehiclePositionDisplayDTO extends VehicleDisplayDTO {
    private int id;
    private CoordinatesDisplayDTO currentCoordinates;
    private CoordinatesDisplayDTO nextCoordinates;
    private LocalDateTime coordinatesChangedAt;
    private long expectedTripTime;
    private boolean rideActive;
}
