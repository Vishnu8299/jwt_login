package com.example.jwtdemo.filter;

import com.example.jwtdemo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);  // Extract token
            String username = jwtUtil.extractClaims(token).getSubject();
            if (username != null && jwtUtil.isTokenValid(token, username)) {
                // Set the authentication context if needed, such as SecurityContext
                // This could be part of the SecurityContext where you can set the user or roles
            }
        }
        return chain.filter(exchange); // Proceed with the next filter
    }
}
