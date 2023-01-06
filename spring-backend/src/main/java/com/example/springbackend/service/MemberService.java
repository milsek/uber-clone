package com.example.springbackend.service;

import com.example.springbackend.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.springbackend.model.Member;

import java.util.Optional;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;


    public boolean isUserBanned(String username){
        Optional<Member> optMember = memberRepository.findByUsername(username);
        return optMember.isPresent() ?  optMember.get().getBanned() : false;
    }
}
