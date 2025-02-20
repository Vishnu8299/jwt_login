package com.example.jwtdemo.model;

import com.example.jwtdemo.model.ProjectStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "projects")
public class Project {
    @Id
    private String id;
    private String name;
    private String description;
    private String userId;
    private String githubUrl;
    private ProjectStatus status;
    private String[] technologies;
    private String createdAt;
    private String updatedAt;

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getUserId() { return userId; }
    public String getGithubUrl() { return githubUrl; }
    public ProjectStatus getStatus() { return status; }
    public String[] getTechnologies() { return technologies; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }
    public void setStatus(ProjectStatus status) { this.status = status; }
    public void setTechnologies(String[] technologies) { this.technologies = technologies; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
