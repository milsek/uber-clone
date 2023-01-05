package com.example.springbackend.dto.paypal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderResponseDTO {
    private String id;
    private OrderStatus status;
    private List<LinkDTO> links;
}
