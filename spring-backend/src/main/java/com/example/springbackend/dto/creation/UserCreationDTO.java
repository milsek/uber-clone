package com.example.springbackend.dto.creation;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class UserCreationDTO {
    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank
    @Size(min = 8, message = "Password must have 8 characters or longer.")
    @Size(max = 256, message = "Password must not be longer than 256 characters.")
    private String password;

    @NotBlank
    @Size(min = 2, max = 20, message = "First name must be longer than 2, and shorter than 20 characters.")
    private String name;

    @NotBlank
    @Size(min = 2, max = 32, message = "Surname must be longer than 2, and shorter than 32 characters.")
    private String surname;

    @NotBlank
    @Pattern(regexp = "[+]?[(]?\\d{3}[)]?[-\\s.]?\\d{3}[-\\s.]?\\d{4,6}", message="Invalid phone number.")
    private String phoneNumber;

    @NotBlank
    private String city;

    private String profilePicture;
}
