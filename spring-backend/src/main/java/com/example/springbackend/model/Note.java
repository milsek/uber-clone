package com.example.springbackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "member_username")
    @JsonIgnore
    Member member;

    @ManyToOne
    @JoinColumn(name = "admin_username")
    Admin admin;

    String content;

}
