package com.example.springbackend.dto.display;

import com.example.springbackend.model.Message;
import com.example.springbackend.model.User;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChatDisplayDTO {
    private LocalDateTime lastReadAdmin;

    private LocalDateTime lastReadMember;

    private User member;

    private List<Message> messages;
}
