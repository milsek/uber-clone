package com.example.springbackend.dto.creation;

import lombok.Data;

@Data
public class DriverRideRejectionCreationDTO extends RideIdDTO {
    private String reason;
}
