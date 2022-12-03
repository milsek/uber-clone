package com.example.springbackend.controller;

import com.example.springbackend.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    TestService testService;

    @GetMapping(path = "test")
    public String test(){
        return testService.test();
    }

    @GetMapping
    public Object currentUser(OAuth2AuthenticationToken oAuth2AuthenticationToken){
        return oAuth2AuthenticationToken.getPrincipal().getAttributes();
    }

}
