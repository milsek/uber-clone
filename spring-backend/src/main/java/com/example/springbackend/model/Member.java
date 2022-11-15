package com.example.springbackend.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.List;

@Entity
@Data
public class Member extends User{

    Boolean blocked;

    @OneToMany(mappedBy = "member")
    private List<Note> notes;

    @OneToOne
    @JoinColumn(name = "chat_id")
    Chat chat;
}
