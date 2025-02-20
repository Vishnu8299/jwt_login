package com.example.jwtdemo.repository;

import com.example.jwtdemo.model.Hackathon;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HackathonRepository extends ReactiveMongoRepository<Hackathon, String> {
    // Basic CRUD operations are automatically provided by ReactiveMongoRepository
}
