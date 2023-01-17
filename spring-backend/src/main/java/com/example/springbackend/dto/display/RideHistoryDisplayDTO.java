package com.example.springbackend.dto.display;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RideHistoryDisplayDTO {
    private Integer id;
    private Double distance;
    private int expectedTime;
    private int price;
    private RouteDisplayDTO route;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

}
