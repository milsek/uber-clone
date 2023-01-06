package com.example.springbackend.dto.display;

import lombok.Data;

@Data
public class PassengerAccountDisplayDTO extends AccountDisplayDTO {
    private double distanceTravelled;
    private int ridesCompleted;
    private int tokenBalance;
}
