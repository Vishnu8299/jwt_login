package com.example.jwtdemo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Document(collection = "users")
public class User {
    @Id
    private String id;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Password is required")
    private String password;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    private UserRole role;
    private String organization;
    private String phoneNumber;
    private boolean isActive;
    private String createdAt;
}
