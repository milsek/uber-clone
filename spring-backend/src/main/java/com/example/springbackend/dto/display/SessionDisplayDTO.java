package com.example.springbackend.dto.display;

import lombok.Data;

@Data
public class SessionDisplayDTO {
    private String username;
    private String name;
    private String surname;
    private String profilePicture;
    private String accountType;
}
