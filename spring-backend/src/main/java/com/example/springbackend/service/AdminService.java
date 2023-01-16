package com.example.springbackend.service;

import com.example.springbackend.model.*;
import com.example.springbackend.repository.MemberRepository;
import com.example.springbackend.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    NoteRepository noteRepository;

    public void banMember(String username){
        Member member = memberRepository.findByUsername(username).get();
        member.setAccountStatus(AccountStatus.TERMINATED);
        memberRepository.save(member);
    }
    public void unbanMember(String username){
        Member member = memberRepository.findByUsername(username).get();
        member.setAccountStatus(AccountStatus.ACTIVE);
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
