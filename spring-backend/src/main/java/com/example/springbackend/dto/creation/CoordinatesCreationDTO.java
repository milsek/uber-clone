package com.example.springbackend.dto.creation;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
public class CoordinatesCreationDTO {
    @Min(-90)
    @Max(90)
    private double lat;
    @Min(-180)
    @Max(180)
    private double lng;
}
