package com.kobe.dinger.service;

import com.kobe.dinger.model.User;
import com.kobe.dinger.repository.*;

import org.springframework.stereotype.Service;

@Service
public class UserService {
    
    private UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;

    }

    public User createUser(String email, String username, String passwordHash){

        if(userRepository.existsByEmail(email)){
            throw new RuntimeException("Email already in use");
        }

       User user = new User(email, username, passwordHash);
       userRepository.save(user);
       return user;
    }

    public User login(String email, String passwordHash){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Email not found"));
        if(user.getPasswordHash().equals(passwordHash)){
            return user;
        } else {
            throw new RuntimeException("Incorrect passowrd!");
        }
    }

}
