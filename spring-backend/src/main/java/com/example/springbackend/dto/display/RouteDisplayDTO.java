package com.example.springbackend.dto.display;

import java.util.List;
import lombok.Data;

@Data
public class RouteDisplayDTO {
    private List<CoordinatesDisplayDTO> coordinates;
    private List<CoordinatesDisplayDTO> waypoints;
}
