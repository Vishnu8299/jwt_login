package com.example.jwtdemo.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${spring.security.jwt.secret}")
    private String secret;

    @Value("${spring.security.jwt.expiration}")
    private long expiration;

    private final Key signingKey;

    public JwtUtil(@Value("${spring.security.jwt.secret}") String secret) {
        this.secret = secret;
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Generate token
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
    }

    // Extract claims from token
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Validate token
    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractClaims(token).getSubject();
        return extractedUsername.equals(username) && !isTokenExpired(token);
    }

    // Check if token expired
    private boolean isTokenExpired(String token) {
        final Date expiration = extractClaims(token).getExpiration();
        return expiration.before(new Date());
    }
}
