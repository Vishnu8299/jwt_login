package com.example.jwtdemo.dto;

import com.example.jwtdemo.validation.PasswordConstraint;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class PasswordResetRequest {
    @NotBlank(message = "Token is required")
    private String token;
    
    @NotBlank(message = "New password is required")
    @PasswordConstraint(message = "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one number and one special character")
    private String newPassword;
    
    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
}
