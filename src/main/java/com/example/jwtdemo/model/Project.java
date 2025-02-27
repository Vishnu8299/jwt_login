package com.example.jwtdemo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "projects")
public class Project {
    @Id
    private String id;
    private String name;
    private String description;
    private String userId;
    private String visibility;
    private boolean addReadme;
    private String gitignoreTemplate;
    private String license;
    private List<ProjectFile> files;
    private String[] technologies;
    private ProjectStatus status;
    private String createdAt;
    private String updatedAt;

    public static class ProjectFile {
        private String filename;
        private String contentType;
        private byte[] data;
        private long size;

        public String getFilename() { return filename; }
        public String getContentType() { return contentType; }
        public byte[] getData() { return data; }
        public long getSize() { return size; }

        public void setFilename(String filename) { this.filename = filename; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        public void setData(byte[] data) { this.data = data; }
        public void setSize(long size) { this.size = size; }
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getUserId() { return userId; }
    public String getVisibility() { return visibility; }
    public boolean isAddReadme() { return addReadme; }
    public String getGitignoreTemplate() { return gitignoreTemplate; }
    public String getLicense() { return license; }
    public List<ProjectFile> getFiles() { return files; }
    public String[] getTechnologies() { return technologies; }
    public ProjectStatus getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setVisibility(String visibility) { this.visibility = visibility; }
    public void setAddReadme(boolean addReadme) { this.addReadme = addReadme; }
    public void setGitignoreTemplate(String gitignoreTemplate) { this.gitignoreTemplate = gitignoreTemplate; }
    public void setLicense(String license) { this.license = license; }
    public void setFiles(List<ProjectFile> files) { this.files = files; }
    public void setTechnologies(String[] technologies) { this.technologies = technologies; }
    public void setStatus(ProjectStatus status) { this.status = status; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
