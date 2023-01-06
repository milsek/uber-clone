package com.example.springbackend.model;

import lombok.Data;
import org.hibernate.annotations.Where;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.util.List;
@Entity
@Where(clause = "banned = false")
public class Member extends User{

    Boolean banned;

    @OneToMany(mappedBy = "member")
    private List<Note> notes;

    public Boolean getBanned() {
        return banned;
    }

    public void setBanned(Boolean banned) {
        this.banned = banned;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }
}
