package com.example.jwtdemo.service;

import com.example.jwtdemo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CustomUserDetailsService implements ReactiveUserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public Mono<UserDetails> findByUsername(String email) {
        return userRepository.findByEmail(email)
            .map(user -> User.withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build())
            .switchIfEmpty(Mono.empty());
    }
}
