package com.example.jwtdemo.controller;

import com.example.jwtdemo.dto.ApiResponse;
import com.example.jwtdemo.dto.PasswordResetRequest;
import com.example.jwtdemo.model.LoginRequest;
import com.example.jwtdemo.model.LoginResponse;
import com.example.jwtdemo.model.User;
import com.example.jwtdemo.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
@Validated
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Mono<ResponseEntity<ApiResponse<LoginResponse>>> login(
            @Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Login attempt for user: {}", loginRequest.getEmail());
        return authService.login(loginRequest)
                .map(response -> ResponseEntity.ok(
                    ApiResponse.<LoginResponse>success(response, "Login successful")
                ))
                .onErrorResume(ex -> {
                    logger.error("Login failed", ex);
                    return Mono.just(ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.<LoginResponse>error("Authentication failed: " + ex.getMessage())));
                });
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<ApiResponse<LoginResponse>>> register(
            @Valid @RequestBody User user) {
        logger.info("Registration attempt for: {} with role: {}", user.getEmail(), user.getRole());
        logger.debug("Full registration data: {}", user);
        
        if (user.getRole() == null) {
            logger.error("User role is null");
            return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<LoginResponse>error("User role is required")));
        }
        
        switch (user.getRole()) {
            case DEVELOPER:
                return authService.registerDeveloper(user)
                    .map(response -> ResponseEntity.ok(ApiResponse.<LoginResponse>success(response, "Developer registered successfully")))
                    .onErrorResume(ex -> {
                        logger.error("Developer registration failed", ex);
                        return Mono.just(ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(ApiResponse.<LoginResponse>error("Registration failed: " + ex.getMessage())));
                    });
            case BUYER:
                return authService.registerBuyer(user)
                    .map(response -> ResponseEntity.ok(ApiResponse.<LoginResponse>success(response, "Buyer registered successfully")))
                    .onErrorResume(ex -> {
                        logger.error("Buyer registration failed", ex);
                        return Mono.just(ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(ApiResponse.<LoginResponse>error("Registration failed: " + ex.getMessage())));
                    });
            default:
                return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<LoginResponse>error("Invalid user role: " + user.getRole())));
        }
    }

    @PostMapping("/register/developer")
    public Mono<ResponseEntity<ApiResponse<LoginResponse>>> registerDeveloper(
            @Valid @RequestBody User user) {
        logger.info("Developer registration attempt for: {}", user.getEmail());
        return authService.registerDeveloper(user)
                .map(response -> ResponseEntity.ok(ApiResponse.<LoginResponse>success(response, "Developer registered successfully")))
                .onErrorResume(ex -> {
                    logger.error("Developer registration failed", ex);
                    return Mono.just(ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.<LoginResponse>error("Registration failed: " + ex.getMessage())));
                });
    }

    @PostMapping("/register/buyer")
    public Mono<ResponseEntity<ApiResponse<LoginResponse>>> registerBuyer(
            @Valid @RequestBody User user) {
        logger.info("Buyer registration attempt for: {}", user.getEmail());
        return authService.registerBuyer(user)
                .map(response -> ResponseEntity.ok(ApiResponse.<LoginResponse>success(response, "Buyer registered successfully")))
                .onErrorResume(ex -> {
                    logger.error("Buyer registration failed", ex);
                    return Mono.just(ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.<LoginResponse>error("Registration failed: " + ex.getMessage())));
                });
    }

    @PostMapping("/refresh-token")
    public Mono<ResponseEntity<ApiResponse<LoginResponse>>> refreshToken(
            @RequestHeader(name = "Authorization", required = true) 
            @Pattern(regexp = "^Bearer [A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_.+/=]*$", 
                    message = "Invalid Bearer token format") 
            String token) {
        logger.info("Token refresh attempt");
        return authService.refreshToken(token.replace("Bearer ", ""))
                .map(response -> ResponseEntity.ok(ApiResponse.<LoginResponse>success(response, "Token refreshed successfully")))
                .onErrorResume(ex -> {
                    logger.error("Token refresh failed", ex);
                    return Mono.just(ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.<LoginResponse>error("Token refresh failed: " + ex.getMessage())));
                });
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<ApiResponse<Void>>> logout(
            @RequestHeader(name = "Authorization", required = true)
            @Pattern(regexp = "^Bearer [A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_.+/=]*$",
                    message = "Invalid Bearer token format")
            String token) {
        logger.info("Logout attempt");
        return authService.logout(token.replace("Bearer ", ""))
                .thenReturn(ResponseEntity.ok(ApiResponse.<Void>success(null, "Logged out successfully")))
                .onErrorResume(ex -> {
                    logger.error("Logout failed", ex);
                    return Mono.just(ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.<Void>error("Logout failed: " + ex.getMessage())));
                });
    }

    @PostMapping("/forgot-password")
    public Mono<ResponseEntity<ApiResponse<Void>>> forgotPassword(
            @RequestParam @Email(message = "Invalid email format") 
            @NotBlank(message = "Email cannot be blank") 
            String email) {
        logger.info("Password reset request for email: {}", email);
        return authService.initiatePasswordReset(email)
                .then(Mono.just(ResponseEntity.ok(
                    ApiResponse.<Void>success(null, "Password reset instructions sent to your email")
                )))
                .onErrorResume(ex -> {
                    logger.error("Password reset initiation failed", ex);
                    return Mono.just(ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.<Void>error("Password reset failed: " + ex.getMessage())));
                });
    }

    @PostMapping("/reset-password")
    public Mono<ResponseEntity<ApiResponse<Void>>> resetPassword(
            @Valid @RequestBody PasswordResetRequest request) {
        logger.info("Password reset attempt");
        return authService.resetPassword(request)
                .then(Mono.just(ResponseEntity.ok(
                    ApiResponse.<Void>success(null, "Password reset successful")
                )))
                .onErrorResume(ex -> {
                    logger.error("Password reset failed", ex);
                    return Mono.just(ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.<Void>error("Password reset failed: " + ex.getMessage())));
                });
    }

    @GetMapping("/verify-reset-token")
    public Mono<ResponseEntity<ApiResponse<Boolean>>> verifyResetToken(
            @RequestParam @NotBlank(message = "Reset token cannot be blank") 
            String token) {
        logger.info("Reset token verification attempt");
        return authService.verifyResetToken(token)
                .map(valid -> ResponseEntity.ok(
                    ApiResponse.<Boolean>success(valid, valid ? "Token is valid" : "Token is invalid or expired")
                ))
                .onErrorResume(ex -> {
                    logger.error("Token verification failed", ex);
                    return Mono.just(ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.<Boolean>error("Token verification failed: " + ex.getMessage())));
                });
    }
}
