package com.example.springbackend.model.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {


    private String oauth2ClientName;
    private OAuth2User oAuth2User;

    public CustomOAuth2User(OAuth2User oAuth2User, String oauth2ClientName) {
        this.oAuth2User = oAuth2User;
        this.oauth2ClientName = oauth2ClientName;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oAuth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return oAuth2User.getAuthorities();
    }

    @Override
    public String getName() {
        return oAuth2User.getAttribute("name");
    }

    public OAuth2User getoAuth2User() {
        return oAuth2User;
    }

    public void setoAuth2User(OAuth2User oAuth2User) {
        this.oAuth2User = oAuth2User;
    }

    public String getEmail() {
        return oAuth2User.getAttribute("email");
    }

    public String getOauth2ClientName() {
        return oauth2ClientName;
    }

    public void setOauth2ClientName(String oauth2ClientName) {
        this.oauth2ClientName = oauth2ClientName;
    }
}
