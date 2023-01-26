package com.example.springbackend.dto.display;

import com.example.springbackend.model.RideStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RideHistoryDisplayDTO {
    private Integer id;
    private Double distance;
    private int expectedTime;
    private int price;
    private RouteDisplayDTO actualRoute;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String startAddress;
    private String destinationAddress;
    private String vehicleType;
    private RideStatus status;
    private double driverRating;
    private double vehicleRating;
}
