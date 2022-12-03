package com.example.springbackend.dto.creation;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;

@Data
@NoArgsConstructor
public class UserCreationDTO {
    private String username;
    private String email;
    private String password;
    private String name;
    private String surname;
    private String phoneNumber;
    private String city;
    private String profilePicture;
}
