package com.example.springbackend.model;

import com.example.springbackend.model.helpClasses.AuthenticationProvider;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Data
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Table(name="users")
public class User implements UserDetails {
    @Id
    private String username;
    private String email;
    private String password;
    private String name;
    private String surname;
    private String phoneNumber;
    private String city;
    private String profilePicture;

    @Enumerated(EnumType.STRING)
    private AuthenticationProvider authenticationProvider;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
