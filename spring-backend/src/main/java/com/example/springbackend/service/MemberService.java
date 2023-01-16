package com.example.springbackend.service;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.springbackend.model.AccountStatus;
import com.example.springbackend.repository.MemberRepository;
import com.example.springbackend.util.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.springbackend.model.Member;

import java.util.Optional;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TokenUtils tokenUtils;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;
    @Autowired
    private UserService userService;


    public boolean isUserActive(String username){
        Optional<Member> optMember = memberRepository.findByUsername(username);
        return optMember.isPresent() ? optMember.get().getAccountStatus().equals(AccountStatus.ACTIVE) : true;
    }
    public void confirmRegistration(String confirmationToken) {
        try {
            DecodedJWT decodedJWT = tokenUtils.verifyToken(confirmationToken);
            Member member = memberRepository.findByUsername(decodedJWT.getSubject()).orElseThrow();
            member.setAccountStatus(AccountStatus.ACTIVE);
            memberRepository.save(member);
        } catch (TokenExpiredException e) {
        }
    }

    public boolean confirmPasswordReset(String token, String newPassword) {
        try{
            DecodedJWT decodedJWT = tokenUtils.verifyToken(token);
            Member member = memberRepository.findByUsername(decodedJWT.getSubject()).orElseThrow();
            member.setPassword(passwordEncoder.encode(newPassword));
            memberRepository.save(member);
            return true;
        } catch (TokenExpiredException e) {
            return false;
        }
    }

    public boolean passwordReset(String mail) {
        if (userService.userExists(mail)) {
            String jwt = tokenUtils.generateConfirmationToken(mail);
            emailService.sendPasswordResetEmail(mail, jwt);
            return true;
        }
        return false;
    }
}
