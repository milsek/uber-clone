package com.example.springbackend.dto.search;

import lombok.Data;

@Data
public class SearchDTO {
    private String name;
    private String surname;
    private String username;
    private Integer page;
}
