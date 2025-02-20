package com.example.jwtdemo.service;

import com.example.jwtdemo.model.Project;
import com.example.jwtdemo.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.Arrays;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    public Mono<Project> createProject(Project project) {
        project.setCreatedAt(LocalDateTime.now().toString());
        project.setUpdatedAt(LocalDateTime.now().toString());
        return projectRepository.save(project);
    }

    public Mono<Project> updateProject(String id, Project project) {
        return projectRepository.findById(id)
                .flatMap(existingProject -> {
                    project.setId(id);
                    project.setUpdatedAt(LocalDateTime.now().toString());
                    return projectRepository.save(project);
                });
    }

    public Flux<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public Flux<Project> getUserProjects(String userId) {
        return projectRepository.findByUserId(userId);
    }

    public Flux<Project> searchProjects(String[] technologies, String keyword) {
        if (technologies == null || technologies.length == 0) {
            return projectRepository.findAll()
                .filter(project -> keyword == null || keyword.isEmpty() ||
                    project.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                    project.getDescription().toLowerCase().contains(keyword.toLowerCase()));
        }
        
        return projectRepository.findAll()
            .filter(project -> {
                boolean matchesTech = Arrays.stream(project.getTechnologies())
                    .anyMatch(tech -> Arrays.asList(technologies).contains(tech));
                boolean matchesKeyword = keyword == null || keyword.isEmpty() ||
                    project.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                    project.getDescription().toLowerCase().contains(keyword.toLowerCase());
                return matchesTech && matchesKeyword;
            });
    }

    public Mono<Project> purchaseProject(String projectId, String userId) {
        return projectRepository.findById(projectId)
            .flatMap(project -> {
                project.setUserId(userId);
                project.setUpdatedAt(LocalDateTime.now().toString());
                return projectRepository.save(project);
            });
    }
}
