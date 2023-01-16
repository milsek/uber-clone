package com.example.springbackend.controller;

import com.example.springbackend.dto.update.LeaveNoteDTO;
import com.example.springbackend.dto.update.UsernameDTO;
import com.example.springbackend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;
    @Autowired
    TestService testService;

    @PostMapping("/ban-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> banUser(@RequestBody UsernameDTO usernameDTO){
        adminService.banMember(usernameDTO.getUsername());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unban-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> unbanUser(@RequestBody UsernameDTO usernameDTO){
        adminService.unbanMember(usernameDTO.getUsername());
        return ResponseEntity.ok().build();
    }
    @PostMapping("/leave-note")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> leaveNote(@RequestBody LeaveNoteDTO leaveNoteDTO){
        adminService.leaveNote(leaveNoteDTO.getContent(),leaveNoteDTO.getUsername());
        return ResponseEntity.ok().build();
    }
}
