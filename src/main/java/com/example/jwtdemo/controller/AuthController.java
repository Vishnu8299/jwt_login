package com.example.jwtdemo.controller;

import com.example.jwtdemo.model.User;
import com.example.jwtdemo.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public Mono<String> register(@RequestBody User user) {
        return authService.register(user);
    }

    @PostMapping("/login")
    public Mono<String> login(@RequestBody User user) {
        return authService.login(user);
    }
}
