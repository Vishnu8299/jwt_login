package com.example.jwtdemo.model;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
    private UserRole role;  // Required for role-based login
}
