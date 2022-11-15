package com.example.springbackend.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
@Data
public class Admin extends User{
    @OneToMany(mappedBy = "admin")
    private List<Note> notes;
}
