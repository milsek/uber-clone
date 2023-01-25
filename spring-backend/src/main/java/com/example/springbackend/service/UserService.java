package com.example.springbackend.service;

import com.example.springbackend.dto.display.AccountDisplayDTO;
import com.example.springbackend.dto.display.SessionDisplayDTO;
import com.example.springbackend.dto.update.UserUpdateDTO;
import com.example.springbackend.model.AccountStatus;
import com.example.springbackend.model.Passenger;
import com.example.springbackend.model.Role;
import com.example.springbackend.model.User;
import com.example.springbackend.model.helpClasses.AuthenticationProvider;
import com.example.springbackend.model.security.CustomOAuth2User;
import com.example.springbackend.repository.PassengerRepository;
import com.example.springbackend.repository.RoleRepository;
import com.example.springbackend.repository.UserRepository;
import com.example.springbackend.security.UserTokenState;
import com.example.springbackend.util.TokenUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    PassengerRepository passengerRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private TokenUtils tokenUtils;
    @Autowired
    private RoleRepository roleRepository;

    public Boolean userExists(String email){
        return userRepository.findByEmail(email).isPresent();
    }

    public Boolean userExistsForCustomRegistration(String email, String username){
        return userRepository.findByEmail(email).isPresent() || userRepository.findByUsername(username).isPresent();
    }

    public UserTokenState processOAuthPostLogin(CustomOAuth2User customOAuth2User) {
        Passenger newUser = new Passenger();
        if (!userExists(customOAuth2User.getEmail())) {
            newUser.setEmail(customOAuth2User.getEmail());
            newUser.setUsername(customOAuth2User.getEmail());
            newUser.setName(customOAuth2User.getName());
            newUser.setSurname("");
            newUser.setProfilePicture("test");
            newUser.setAuthenticationProvider(AuthenticationProvider.valueOf(customOAuth2User.getOauth2ClientName().toUpperCase()));
            newUser.setPassword(passengerRepository.findByEmail("passenger1@noemail.com").get().getPassword());
            newUser.setAccountStatus(AccountStatus.ACTIVE);
            List<Role> rolesList = new ArrayList<Role>();
            rolesList.add(roleRepository.findById(Long.valueOf(3)));
            System.out.println(rolesList.size());
            newUser.setRoles(rolesList);
            passengerRepository.save(newUser);
        }
        String jwt = tokenUtils.generateToken(customOAuth2User.getEmail());
        int expiresIn = tokenUtils.getExpiredIn();
        return new UserTokenState(jwt, expiresIn);
    }

    public AccountDisplayDTO getAccount(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return modelMapper.map(user, AccountDisplayDTO.class);
    }

    public SessionDisplayDTO whoAmI(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return modelMapper.map(user, SessionDisplayDTO.class);
    }

    public boolean updateUser(UserUpdateDTO userUpdateDTO) {
        Optional<User> optUser = userRepository.findByUsername(userUpdateDTO.getUsername());
        if(optUser.isPresent()){
            User user = optUser.get();
            user.setCity(userUpdateDTO.getCity());
            user.setName(userUpdateDTO.getName());
            user.setSurname(userUpdateDTO.getSurname());
            user.setPhoneNumber(userUpdateDTO.getPhoneNumber());
            user.setProfilePicture(userUpdateDTO.getProfilePicture());
            userRepository.save(user);
            return true;
        }
        return false;
    }
}
