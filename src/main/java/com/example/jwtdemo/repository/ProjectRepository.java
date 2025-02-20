package com.example.jwtdemo.repository;

import com.example.jwtdemo.model.Project;
import com.example.jwtdemo.model.ProjectStatus;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ProjectRepository extends ReactiveCrudRepository<Project, String> {
    // Add custom query methods here if needed
    Flux<Project> findByUserId(String userId);
    Mono<Long> countByStatus(ProjectStatus status);
}
