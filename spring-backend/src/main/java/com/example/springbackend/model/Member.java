package com.example.springbackend.model;

import lombok.Data;
import org.hibernate.annotations.Where;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.util.List;
@Entity
@Where(clause = "banned = false")
public class Member extends User{

    private AccountStatus accountStatus;

    @OneToMany(mappedBy = "member")
    private List<Note> notes;

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }
}
