package com.example.jwtdemo.controller;

import com.example.jwtdemo.model.Hackathon;
import com.example.jwtdemo.service.HackathonService;
import com.example.jwtdemo.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/hackathons")
@Validated
public class HackathonController {
    private static final Logger logger = LoggerFactory.getLogger(HackathonController.class);

    @Autowired
    private HackathonService hackathonService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<ApiResponse<Hackathon>>> createHackathon(
            @Valid @RequestBody Hackathon hackathon) {
        logger.info("Creating hackathon: {}", hackathon.getName());
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

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<Flux<Hackathon>>>> getAllHackathons() {
        logger.info("Retrieving all hackathons");
        return Mono.just(ResponseEntity.ok(
            ApiResponse.success(hackathonService.getAllHackathons(), "Hackathons retrieved successfully")
        )).onErrorResume(ex -> {
            logger.error("Failed to retrieve hackathons", ex);
            return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve hackathons: " + ex.getMessage())));
        });
    }

    @PostMapping("/{hackathonId}/register")
    @PreAuthorize("hasRole('DEVELOPER')")
    public Mono<ResponseEntity<ApiResponse<Void>>> registerParticipant(
            @PathVariable @NotBlank(message = "Hackathon ID cannot be blank") String hackathonId,
            @RequestParam @NotBlank(message = "User ID cannot be blank") String userId) {
        logger.info("Registering participant {} for hackathon {}", userId, hackathonId);
        return hackathonService.registerParticipant(hackathonId, userId)
                .map(hackathon -> ResponseEntity.ok(
                    ApiResponse.<Void>success(null, "Participant registered successfully")
                ))
                .onErrorResume(ex -> {
                    logger.error("Failed to register participant", ex);
                    return Mono.just(ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Participant registration failed: " + ex.getMessage())));
                });
    }
}
