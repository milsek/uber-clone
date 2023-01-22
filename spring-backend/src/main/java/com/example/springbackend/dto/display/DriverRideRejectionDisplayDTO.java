package com.example.springbackend.dto.display;

import lombok.Data;

@Data
public class DriverRideRejectionDisplayDTO {
    private Integer id;
    private String reason;
    private DriverNanoDisplayDTO driver;
}
