package com.example.springbackend.websocket;

import com.example.springbackend.model.User;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Builder
@ToString
public class WSMessage {
    @Getter
    private MessageType type;
    @Getter
    private String content;
    @Getter
    private String sender;
    @Getter
    private String receiver;
    @Getter
    private LocalDateTime sentDateTime;
}
