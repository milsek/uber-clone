package com.example.springbackend.dto.creation;

import lombok.Data;

import java.util.List;

@Data
public class SplitFareRideCreationDTO extends BasicRideCreationDTO{
    List<String> usersToPay;
}
