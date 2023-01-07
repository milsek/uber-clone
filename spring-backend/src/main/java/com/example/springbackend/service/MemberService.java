package com.example.springbackend.service;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.springbackend.model.AccountStatus;
import com.example.springbackend.model.User;
import com.example.springbackend.repository.MemberRepository;
import com.example.springbackend.util.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.springbackend.model.Member;

import java.util.Optional;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TokenUtils tokenUtils;


    public boolean isUserActive(String username){
        Optional<Member> optMember = memberRepository.findByUsername(username);
        return optMember.isPresent() ? optMember.get().getAccountStatus().equals(AccountStatus.ACTIVE) : true;
    }
    public void confirmRegistration(String confirmationToken) {
        try {
            DecodedJWT decodedJWT = tokenUtils.verifyToken(confirmationToken);
            System.out.println(decodedJWT);
            System.out.println(decodedJWT.getSubject());
            Member member = memberRepository.findByUsername(decodedJWT.getSubject()).orElseThrow();
            member.setAccountStatus(AccountStatus.ACTIVE);
            memberRepository.save(member);
        } catch (TokenExpiredException e) {
        }
    }
}
