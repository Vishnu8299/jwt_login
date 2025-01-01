package com.example.jwtdemo.service;

import com.example.jwtdemo.model.User;
import com.example.jwtdemo.repository.UserRepository;
import com.example.jwtdemo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public Mono<String> register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user)
                .map(savedUser -> "User registered successfully");
    }

    public Mono<String> login(User user) {
        return userRepository.findByUsername(user.getUsername())
                .filter(dbUser -> passwordEncoder.matches(user.getPassword(), dbUser.getPassword()))
                .map(dbUser -> jwtUtil.generateToken(dbUser.getUsername()))
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid username or password")));
    }
}
