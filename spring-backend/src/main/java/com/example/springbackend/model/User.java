package com.example.springbackend.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Table(name="users")
public class User {
    @Id
    private String username;
    private String email;
    private String password;
    private String name;
    private String surname;
    private String phoneNumber;
    private String city;
    private String profilePicture;

}
