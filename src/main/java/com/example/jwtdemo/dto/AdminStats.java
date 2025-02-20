package com.example.jwtdemo.dto;

public class AdminStats {
    private long totalUsers;
    private long totalProjects;
    private long totalHackathons;
    private long activeProjects;
    private long completedProjects;

    // Getters
    public long getTotalUsers() { return totalUsers; }
    public long getTotalProjects() { return totalProjects; }
    public long getTotalHackathons() { return totalHackathons; }
    public long getActiveProjects() { return activeProjects; }
    public long getCompletedProjects() { return completedProjects; }

    // Setters
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
    public void setTotalProjects(long totalProjects) { this.totalProjects = totalProjects; }
    public void setTotalHackathons(long totalHackathons) { this.totalHackathons = totalHackathons; }
    public void setActiveProjects(long activeProjects) { this.activeProjects = activeProjects; }
    public void setCompletedProjects(long completedProjects) { this.completedProjects = completedProjects; }
}
