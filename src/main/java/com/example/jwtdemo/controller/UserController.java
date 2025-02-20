package com.example.jwtdemo.controller;

import com.example.jwtdemo.model.User;
import com.example.jwtdemo.service.UserService;
import com.example.jwtdemo.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @PostMapping("/developer")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<ApiResponse<User>>> registerDeveloper(
            @Valid @RequestBody User user) {
        logger.info("Registering developer: {}", user.getEmail());
        return userService.registerUser(user)
                .map(registeredUser -> ResponseEntity.ok(
                    ApiResponse.success(registeredUser, "Developer registered successfully")
                ))
                .onErrorResume(ex -> {
                    logger.error("Failed to register developer", ex);
                    return Mono.just(ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Developer registration failed: " + ex.getMessage())));
                });
    }

    @PostMapping("/buyer")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<ApiResponse<User>>> registerBuyer(
            @Valid @RequestBody User user) {
        logger.info("Registering buyer: {}", user.getEmail());
        return userService.registerUser(user)
                .map(registeredUser -> ResponseEntity.ok(
                    ApiResponse.success(registeredUser, "Buyer registered successfully")
                ))
                .onErrorResume(ex -> {
                    logger.error("Failed to register buyer", ex);
                    return Mono.just(ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Buyer registration failed: " + ex.getMessage())));
                });
    }

    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<ApiResponse<User>>> registerAdmin(
            @Valid @RequestBody User user) {
        logger.info("Registering admin: {}", user.getEmail());
        return userService.registerUser(user)
                .map(registeredUser -> ResponseEntity.ok(
                    ApiResponse.success(registeredUser, "Admin registered successfully")
                ))
                .onErrorResume(ex -> {
                    logger.error("Failed to register admin", ex);
                    return Mono.just(ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Admin registration failed: " + ex.getMessage())));
                });
    }

    @GetMapping("/current")
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<ApiResponse<User>>> getCurrentUser(
            @RequestHeader("Authorization") String token) {
        logger.info("Retrieving current user profile");
        return userService.getCurrentUser(token.replace("Bearer ", ""))
                .map(user -> ResponseEntity.ok(
                    ApiResponse.success(user, "Current user profile retrieved successfully")
                ))
                .onErrorResume(ex -> {
                    logger.error("Failed to retrieve current user profile", ex);
                    return Mono.just(ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Failed to retrieve current user: " + ex.getMessage())));
                });
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<ApiResponse<User>>> getUserProfile(
            @RequestParam @NotBlank(message = "User ID cannot be blank") String userId) {
        logger.info("Retrieving profile for user: {}", userId);
        return userService.findById(userId)
                .map(user -> ResponseEntity.ok(
                    ApiResponse.success(user, "User profile retrieved successfully")
                ))
                .onErrorResume(ex -> {
                    logger.error("Failed to retrieve user profile", ex);
                    return Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("User profile not found: " + ex.getMessage())));
                });
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<ApiResponse<User>>> updateProfile(
            @Valid @RequestBody User userData, 
            @RequestHeader("Authorization") String token) {
        logger.info("Updating user profile");
        return userService.updateProfile(userData, token)
                .map(updatedUser -> ResponseEntity.ok(
                    ApiResponse.success(updatedUser, "User profile updated successfully")
                ))
                .onErrorResume(ex -> {
                    logger.error("Failed to update user profile", ex);
                    return Mono.just(ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("User profile update failed: " + ex.getMessage())));
                });
    }

    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<ApiResponse<User>>> updateUserStatus(
            @PathVariable @NotBlank(message = "User ID cannot be blank") String userId,
            @RequestParam boolean active) {
        logger.info("Updating user status for user: {}", userId);
        return userService.updateUserStatus(userId, active)
                .map(updatedUser -> ResponseEntity.ok(
                    ApiResponse.success(updatedUser, "User status updated successfully")
                ))
                .onErrorResume(ex -> {
                    logger.error("Failed to update user status", ex);
                    return Mono.just(ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("User status update failed: " + ex.getMessage())));
                });
    }

    @GetMapping("/developers")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<ApiResponse<Flux<User>>>> getAllDevelopers() {
        logger.info("Retrieving all developers");
        return Mono.just(ResponseEntity.ok(
            ApiResponse.success(userService.getAllDevelopers(), "Developers retrieved successfully")
        )).onErrorResume(ex -> {
            logger.error("Failed to retrieve developers", ex);
            return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve developers: " + ex.getMessage())));
        });
    }

    @GetMapping("/buyers")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<ApiResponse<Flux<User>>>> getAllBuyers() {
        logger.info("Retrieving all buyers");
        return Mono.just(ResponseEntity.ok(
            ApiResponse.success(userService.getAllBuyers(), "Buyers retrieved successfully")
        )).onErrorResume(ex -> {
            logger.error("Failed to retrieve buyers", ex);
            return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve buyers: " + ex.getMessage())));
        });
    }
}
