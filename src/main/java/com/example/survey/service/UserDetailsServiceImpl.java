package com.example.survey.service;

import com.example.survey.model.User;
import com.example.survey.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                Collections.emptyList() // Authorities (Roles) can be added here if needed
        );
    }
    
    public User getCurrentUser() {
        return userRepository.findByUsernameOrEmail(
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName(),
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName()
        ).orElse(null);
    }
}
