package com.example.springbackend.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Double distance;
    private int expectedTime;
    private String cancelled;
    private Boolean rejected;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;
    private Boolean driverInconsistency;
    private int price;

    @ManyToOne
    @JoinColumn
    private Route actualRoute;

    @ManyToOne
    @JoinColumn
    private Route expectedRoute;
}
