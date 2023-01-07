package com.example.springbackend.dto.creation;

import lombok.Data;

import javax.validation.constraints.Size;
import java.util.List;

@Data
public class RouteCreationDTO {
    @Size(min = 2)
    private List<CoordinatesCreationDTO> coordinates;
    @Size(min = 2, message = "At least two waypoints are required for a ride.")
    private List<CoordinatesCreationDTO> waypoints;
}
