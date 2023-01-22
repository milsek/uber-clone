package com.example.springbackend.dto.creation;

import lombok.Data;

@Data
public class DriverRideRejectionVerdictCreationDTO extends RideIdDTO {
    boolean accepted;
}
