package com.example.jwtdemo.repository;

import com.example.jwtdemo.model.User;
import com.example.jwtdemo.model.UserRole;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveMongoRepository<User, String> {
    Mono<User> findByEmail(String email);
    Mono<User> findByEmailAndRole(String email, UserRole role);
    Flux<User> findByRole(UserRole role);
}
