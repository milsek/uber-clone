package com.example.springbackend.dto.display;

import lombok.Data;

@Data
public class UserDisplayDTO {
    private String username;
    private String name;
    private String surname;
    private String phoneNumber;
    private String city;
    private String profilePicture;
}
