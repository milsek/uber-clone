package com.example.springbackend.service;

import com.example.springbackend.model.Member;
import com.example.springbackend.repository.MemberRepository;
import com.example.springbackend.repository.RoleRepository;
import com.example.springbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.springbackend.model.User;

@Service
public class AdminService {

@Autowired
    MemberRepository memberRepository;

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
}
