package com.example.springbackend.model;

import lombok.Data;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.util.List;

@Entity
public class Member extends User{

    Boolean blocked;

    @OneToMany(mappedBy = "member")
    private List<Note> notes;

    public Boolean getBlocked() {
        return blocked;
    }
    public void setBlocked(Boolean blocked) {
        this.blocked = blocked;
    }
}
