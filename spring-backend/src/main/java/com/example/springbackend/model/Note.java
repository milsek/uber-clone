package com.example.springbackend.model;

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
    Member member;

    @ManyToOne
    @JoinColumn(name = "admin_username")
    Admin admin;

    String content;

}
