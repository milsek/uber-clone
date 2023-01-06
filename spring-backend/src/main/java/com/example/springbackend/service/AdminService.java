package com.example.springbackend.service;

import com.example.springbackend.model.Admin;
import com.example.springbackend.model.Member;
import com.example.springbackend.model.Note;
import com.example.springbackend.repository.MemberRepository;
import com.example.springbackend.repository.NoteRepository;
import com.example.springbackend.repository.RoleRepository;
import com.example.springbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.example.springbackend.model.User;

@Service
public class AdminService {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    NoteRepository noteRepository;

    public void banMember(String username){
        Member member = memberRepository.findByUsername(username).get();
        member.setBanned(true);
        memberRepository.save(member);
    }
    public void unbanMember(String username){
        Member member = memberRepository.findByUsername(username).get();
        member.setBanned(false);
        memberRepository.save(member);
    }

    public void leaveNote(String content,String username){
        Member member = memberRepository.findByUsername(username).get();
        Note note = new Note();
        note.setMember(member);
        note.setContent(content);
        note.setAdmin( (Admin) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        noteRepository.save(note);
    }
}
