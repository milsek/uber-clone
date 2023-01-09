package com.example.springbackend.service;

import com.example.springbackend.dto.creation.BasicChatUpdateDTO;
import com.example.springbackend.dto.display.ChatDisplayDTO;
import com.example.springbackend.model.Chat;
import com.example.springbackend.model.Message;
import com.example.springbackend.model.User;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.example.springbackend.repository.ChatRepository;
import com.example.springbackend.repository.UserRepository;
import com.example.springbackend.repository.MessageRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ChatService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    ChatRepository chatRepository;
    @Autowired
    MessageRepository messageRepository;
    @Autowired
    private ModelMapper modelMapper;

    public ChatDisplayDTO getUserChat(String username) {
        Optional<User> u = userRepository.findByUsername(username);
        if (u.isPresent()) {
            Optional<Chat> chat = chatRepository.findByMember(u.get());
            if (chat.isPresent()) {
                return modelMapper.map(chat.get(), ChatDisplayDTO.class);
            }
            Chat newChat = new Chat();
            newChat.setMember(u.get());
            newChat.setLastReadAdmin(LocalDateTime.now());
            newChat.setLastReadMember(LocalDateTime.now());
            newChat.setMessages(new ArrayList<>());
            Message firstMessage = new Message();
            firstMessage.setChat(newChat);
            firstMessage.setSender("admin");
            firstMessage.setContent("Hi, do you need any help?");
            firstMessage.setSentDateTime(LocalDateTime.now());
            chatRepository.save(newChat);
            messageRepository.save(firstMessage);
            newChat.getMessages().add(firstMessage);
            return modelMapper.map(newChat, ChatDisplayDTO.class);
        }
        return null;
    }

    public List<ChatDisplayDTO> getAllChats() {
        List<User> users = userRepository.findAll();
        List<ChatDisplayDTO> chats = new ArrayList<>();
        for (User u : users) {
            if (!u.getRoles().get(0).getName().equals("ROLE_ADMIN")) {
                chats.add(getUserChat(u.getUsername()));
            }
        }
        return chats;
    }

    public void updateLastRead(BasicChatUpdateDTO dto) {
        Chat chat = chatRepository.findByMember(userRepository.findByUsername(dto.getUsername()).get()).get();
        if (dto.getType().equals("admin")) {
            chat.setLastReadAdmin(LocalDateTime.now());
        } else {
            chat.setLastReadMember(LocalDateTime.now());
        }
        chatRepository.save(chat);
    }
}
