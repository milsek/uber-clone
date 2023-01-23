package com.example.springbackend.dto.display;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RideSimpleDisplayDTO {
    private Integer id;
    private Double distance;
    private int expectedTime;
    private LocalDateTime createdAt;
    private int price;
    private DriverSimpleDisplayDTO driver;
    private RouteDisplayDTO route;
    private boolean allConfirmed;
    private boolean passengerConfirmed;
    private String status;
    private String startAddress;
    private String destinationAddress;
    private int delayInMinutes;
}
