package com.example.springbackend.model;

import lombok.Data;
import org.hibernate.annotations.Fetch;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDateTime lastReadAdmin;

    private LocalDateTime lastReadMember;

    @OneToOne
    @JoinColumn(name = "member_username")
    private User member;

    @OneToMany(mappedBy = "chat")
    private List<Message> messages;
}
