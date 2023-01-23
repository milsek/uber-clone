package com.example.springbackend.dto.display;

import com.example.springbackend.model.Passenger;
import lombok.Data;

import java.util.List;

@Data
public class PassengerSearchDisplayDTO {
    private List<Passenger> passengers;
    private Integer numberOfPassengers;
}
