package com.example.springbackend.controller;

import com.example.springbackend.dto.creation.BasicChatUpdateDTO;
import com.example.springbackend.dto.creation.BasicRideCreationDTO;
import com.example.springbackend.dto.display.ChatDisplayDTO;
import com.example.springbackend.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "/api/chat", produces = MediaType.APPLICATION_JSON_VALUE)
public class ChatController {
    @Autowired
    private ChatService chatService;

    @GetMapping("/{username}")
    public ResponseEntity<ChatDisplayDTO> getByUsername(@PathVariable String username) {
        return ResponseEntity.ok(chatService.getUserChat(username));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ChatDisplayDTO>> getAllChats() {
        return ResponseEntity.ok(chatService.getAllChats());
    }

    @PostMapping("/updateRead")
    public void updateRead(@Valid @RequestBody BasicChatUpdateDTO dto) {
        chatService.updateLastRead(dto);
    }
}