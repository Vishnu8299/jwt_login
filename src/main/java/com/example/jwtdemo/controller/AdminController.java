package com.example.jwtdemo.controller;

import com.example.jwtdemo.model.User;
import com.example.jwtdemo.model.Hackathon;
import com.example.jwtdemo.service.UserService;
import com.example.jwtdemo.service.HackathonService;
import com.example.jwtdemo.service.AdminService;
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

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private HackathonService hackathonService;

    @Autowired
    private AdminService adminService;

    @GetMapping("/users")
    public Mono<ResponseEntity<ApiResponse<Flux<User>>>> getAllUsers() {
        logger.info("Retrieving all users");
        return Mono.just(ResponseEntity.ok(
            ApiResponse.success(userService.getAllUsers(), "Users retrieved successfully")
        )).onErrorResume(ex -> {
            logger.error("Failed to retrieve users", ex);
            return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve users: " + ex.getMessage())));
        });
    }

    @GetMapping("/stats")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> getStats() {
        logger.info("Retrieving system statistics");
        return adminService.getAdminStats()
            .map(adminStats -> {
                Map<String, Object> stats = new HashMap<>();
                stats.put("totalUsers", adminStats.getTotalUsers());
                stats.put("totalProjects", adminStats.getTotalProjects());
                stats.put("totalHackathons", adminStats.getTotalHackathons());
                stats.put("activeProjects", adminStats.getActiveProjects());
                stats.put("completedProjects", adminStats.getCompletedProjects());
                
                return ResponseEntity.ok(ApiResponse.success(stats, "System statistics retrieved successfully"));
            })
            .onErrorResume(ex -> {
                logger.error("Failed to retrieve system statistics", ex);
                return Mono.just(ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve system statistics: " + ex.getMessage())));
            });
    }

    @PostMapping("/hackathons")
    public Mono<ResponseEntity<ApiResponse<Hackathon>>> createHackathon(
            @Valid @RequestBody Hackathon hackathon) {
        logger.info("Creating new hackathon: {}", hackathon.getName());
        return hackathonService.createHackathon(hackathon)
                .map(createdHackathon -> ResponseEntity.ok(
                    ApiResponse.success(createdHackathon, "Hackathon created successfully")
                ))
                .onErrorResume(ex -> {
                    logger.error("Failed to create hackathon", ex);
                    return Mono.just(ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Hackathon creation failed: " + ex.getMessage())));
                });
    }
}
