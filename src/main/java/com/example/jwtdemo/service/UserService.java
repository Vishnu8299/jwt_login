package com.example.jwtdemo.service;

import com.example.jwtdemo.model.User;
import com.example.jwtdemo.model.UserRole;
import com.example.jwtdemo.repository.UserRepository;
import com.example.jwtdemo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public Mono<User> registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now().toString());
        return userRepository.save(user);
    }

    public Mono<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Mono<User> findById(String id) {
        return userRepository.findById(id);
    }

    public Flux<User> findByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    public Flux<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Mono<User> updateUserStatus(String userId, boolean active) {
        return userRepository.findById(userId)
                .flatMap(user -> {
                    user.setActive(active);
                    return userRepository.save(user);
                });
    }

    public Mono<User> getCurrentUser(String token) {
        String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
        return userRepository.findByEmail(username);
    }

    public Mono<User> updateProfile(User userData, String token) {
        String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
        return userRepository.findByEmail(username)
            .flatMap(existingUser -> {
                existingUser.setName(userData.getName());
                existingUser.setEmail(userData.getEmail());
                // Don't update sensitive fields like password or role
                return userRepository.save(existingUser);
            });
    }

    public Mono<User> updateUserProfile(User userData, String token) {
        return updateProfile(userData, token);
    }

    public Mono<Long> countUsers() {
        return userRepository.count();
    }

    public Mono<List<Map<String, String>>> getRecentActivities() {
        return userRepository.findAll()
            .take(5)
            .map(user -> Map.of(
                "title", "User Registration",
                "description", user.getEmail(),
                "time", "Recently"
            ))
            .collectList();
    }

    public Mono<List<Map<String, String>>> getPendingApprovals() {
        return Mono.just(Arrays.asList(
            Map.of(
                "title", "Repository Approval",
                "description", "E-commerce Template"
            ),
            Map.of(
                "title", "Developer Application",
                "description", "Mark Johnson"
            )
        ));
    }

    public Flux<User> getAllDevelopers() {
        return userRepository.findByRole(UserRole.DEVELOPER);
    }

    public Flux<User> getAllBuyers() {
        return userRepository.findByRole(UserRole.BUYER);
    }
}
