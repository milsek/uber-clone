package com.example.springbackend.dto.display;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DriverRideDisplayDTO {
    private Integer id;
    private Double distance;
    private int expectedTime;
    private LocalDateTime createdAt;
    private int price;
    private List<PassengerDisplayDTO> passengers;
    private RouteDisplayDTO route;
    private String status;
    private String startAddress;
    private String destinationAddress;
}
