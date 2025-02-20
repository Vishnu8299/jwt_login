package com.example.jwtdemo.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private UserRole role;
    private String userId;
    private String name;
}
