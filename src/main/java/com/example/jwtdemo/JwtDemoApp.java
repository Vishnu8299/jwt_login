package com.example.jwtdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.validation.annotation.Validated;

@SpringBootApplication
@Validated
public class JwtDemoApp {
    public static void main(String[] args) {
        SpringApplication.run(JwtDemoApp.class, args);
    }
}
