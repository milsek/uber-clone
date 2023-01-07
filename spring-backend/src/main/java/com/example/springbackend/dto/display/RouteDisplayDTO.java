package com.example.springbackend.dto.display;

import java.util.List;
import lombok.Data;

@Data
public class RouteDisplayDTO {
    private Integer id;
    private List<CoordinatesDisplayDTO> coordinates;
    private List<CoordinatesDisplayDTO> waypoints;
}
