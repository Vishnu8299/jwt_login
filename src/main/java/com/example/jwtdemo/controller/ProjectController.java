package com.example.jwtdemo.controller;

import com.example.jwtdemo.model.Project;
import com.example.jwtdemo.model.ProjectStatus;
import com.example.jwtdemo.service.ProjectService;
import com.example.jwtdemo.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    private ProjectService projectService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('DEVELOPER')")
    public Mono<ResponseEntity<ApiResponse<Project>>> createProject(
            @RequestPart("name") String name,
            @RequestPart("description") String description,
            @RequestPart("visibility") String visibility,
            @RequestPart("addReadme") String addReadme,
            @RequestPart("gitignoreTemplate") String gitignoreTemplate,
            @RequestPart("license") String license,
            @RequestPart(value = "files", required = false) Flux<FilePart> files,
            Authentication authentication) {
        
        logger.info("Creating project with name: {} and user: {}", name, authentication.getName());
        
        Project project = new Project();
        project.setName(name);
        project.setDescription(description);
        project.setVisibility(visibility);
        project.setAddReadme(Boolean.parseBoolean(addReadme));
        project.setGitignoreTemplate(gitignoreTemplate);
        project.setLicense(license);
        project.setUserId(authentication.getName());
        project.setStatus(ProjectStatus.PUBLISHED);
        project.setFiles(new ArrayList<>());

        // Process files if any
        if (files != null) {
            logger.info("Processing files for project: {}", name);
            return files
                .flatMap(filePart -> {
                    logger.info("Processing file: {} of type: {}", 
                        filePart.filename(), 
                        filePart.headers().getContentType());
                    
                    return filePart.content()
                        .reduce(new byte[0], (acc, dataBuffer) -> {
                            byte[] data = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(data);
                            byte[] result = new byte[acc.length + data.length];
                            System.arraycopy(acc, 0, result, 0, acc.length);
                            System.arraycopy(data, 0, result, acc.length, data.length);
                            return result;
                        })
                        .map(bytes -> {
                            Project.ProjectFile projectFile = new Project.ProjectFile();
                            projectFile.setFilename(filePart.filename());
                            projectFile.setContentType(filePart.headers().getContentType().toString());
                            projectFile.setData(bytes);
                            projectFile.setSize(bytes.length);
                            logger.info("File processed: {} (size: {} bytes)", 
                                filePart.filename(), bytes.length);
                            return projectFile;
                        });
                })
                .collectList()
                .flatMap(projectFiles -> {
                    project.setFiles(projectFiles);
                    logger.info("Saving project {} with {} files", name, projectFiles.size());
                    return projectService.createProject(project);
                })
                .map(createdProject -> {
                    logger.info("Project created successfully: {}", createdProject.getId());
                    return ResponseEntity.ok(
                        ApiResponse.success(createdProject, "Project created successfully")
                    );
                })
                .onErrorResume(ex -> {
                    logger.error("Failed to create project: {}", ex.getMessage(), ex);
                    return Mono.just(ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Project creation failed: " + ex.getMessage())));
                });
        }

        // If no files, just create the project
        logger.info("Creating project without files: {}", name);
        return projectService.createProject(project)
                .map(createdProject -> {
                    logger.info("Project created successfully: {}", createdProject.getId());
                    return ResponseEntity.ok(
                        ApiResponse.success(createdProject, "Project created successfully")
                    );
                })
                .onErrorResume(ex -> {
                    logger.error("Failed to create project: {}", ex.getMessage(), ex);
                    return Mono.just(ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Project creation failed: " + ex.getMessage())));
                });
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DEVELOPER')")
    public Mono<ResponseEntity<ApiResponse<Project>>> updateProject(
            @PathVariable @NotBlank(message = "Project ID cannot be blank") String id, 
            @Valid @RequestBody Project project) {
        logger.info("Updating project: {}", id);
        return projectService.updateProject(id, project)
                .map(updatedProject -> ResponseEntity.ok(
                    ApiResponse.success(updatedProject, "Project updated successfully")
                ))
                .onErrorResume(ex -> {
                    logger.error("Failed to update project", ex);
                    return Mono.just(ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Project update failed: " + ex.getMessage())));
                });
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<Flux<Project>>>> getAllProjects() {
        logger.info("Retrieving all projects");
        return Mono.just(ResponseEntity.ok(
            ApiResponse.success(projectService.getAllProjects(), "Projects retrieved successfully")
        )).onErrorResume(ex -> {
            logger.error("Failed to retrieve projects", ex);
            return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve projects: " + ex.getMessage())));
        });
    }

    @GetMapping("/developer/{userId}")
    @PreAuthorize("hasRole('DEVELOPER')")
    public Mono<ResponseEntity<ApiResponse<Flux<Project>>>> getDeveloperProjects(
            @PathVariable @NotBlank(message = "User ID cannot be blank") String userId) {
        logger.info("Retrieving projects for developer: {}", userId);
        return Mono.just(ResponseEntity.ok(
            ApiResponse.success(projectService.getUserProjects(userId), "Developer projects retrieved successfully")
        )).onErrorResume(ex -> {
            logger.error("Failed to retrieve developer projects", ex);
            return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve developer projects: " + ex.getMessage())));
        });
    }

    @GetMapping("/search")
    public Mono<ResponseEntity<ApiResponse<Flux<Project>>>> searchProjects(
            @RequestParam(required = false) String[] technologies,
            @RequestParam(required = false) String status) {
        logger.info("Searching projects with technologies: {}, status: {}", technologies, status);
        return Mono.just(ResponseEntity.ok(
            ApiResponse.success(
                projectService.searchProjects(technologies, status), 
                "Projects searched successfully"
            )
        )).onErrorResume(ex -> {
            logger.error("Failed to search projects", ex);
            return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Project search failed: " + ex.getMessage())));
        });
    }

    @PostMapping("/{projectId}/purchase")
    @PreAuthorize("hasRole('BUYER')")
    public Mono<ResponseEntity<ApiResponse<Void>>> purchaseProject(
            @PathVariable @NotBlank(message = "Project ID cannot be blank") String projectId,
            @RequestParam @NotBlank(message = "Buyer ID cannot be blank") String buyerId) {
        logger.info("Purchasing project {} by buyer {}", projectId, buyerId);
        return projectService.purchaseProject(projectId, buyerId)
                .then(Mono.just(ResponseEntity.ok(
                    ApiResponse.success(Void.class, null, "Project purchased successfully")
                )))
                .onErrorResume(ex -> {
                    logger.error("Failed to purchase project", ex);
                    return Mono.just(ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Project purchase failed: " + ex.getMessage())));
                });
    }
}
