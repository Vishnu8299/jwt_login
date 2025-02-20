package com.example.jwtdemo.service;

import com.example.jwtdemo.dto.AdminStats;
import com.example.jwtdemo.model.ProjectStatus;
import com.example.jwtdemo.repository.UserRepository;
import com.example.jwtdemo.repository.ProjectRepository;
import com.example.jwtdemo.repository.HackathonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private HackathonRepository hackathonRepository;

    public Mono<AdminStats> getAdminStats() {
        AdminStats stats = new AdminStats();

        return Mono.zip(
            userRepository.count(),
            projectRepository.count(),
            hackathonRepository.count(),
            projectRepository.countByStatus(ProjectStatus.PUBLISHED),
            projectRepository.countByStatus(ProjectStatus.ARCHIVED)
        ).map(tuple -> {
            stats.setTotalUsers(tuple.getT1());
            stats.setTotalProjects(tuple.getT2());
            stats.setTotalHackathons(tuple.getT3());
            stats.setActiveProjects(tuple.getT4());
            stats.setCompletedProjects(tuple.getT5());
            return stats;
        });
    }
}
