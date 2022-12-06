package com.example.springbackend.dto.display;

import lombok.Data;

@Data
public class AccountDisplayDTO {
    private String username;
    private String email;
    private String name;
    private String surname;
    private String phoneNumber;
    private String city;
    private String profilePicture;
    private String accountType;
}
