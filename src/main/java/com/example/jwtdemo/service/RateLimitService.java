package com.example.jwtdemo.service;

import com.example.jwtdemo.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.Duration;

@Service
public class RateLimitService {
    private static final String RATE_LIMIT_PREFIX = "rate:";
    private static final int MAX_ATTEMPTS = 5;
    private static final int BLOCK_DURATION_SECONDS = 3600; // 1 hour
    
    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;

    public Mono<Void> checkRateLimit(String key, String action) {
        String rateKey = RATE_LIMIT_PREFIX + action + ":" + key;
        
        return redisTemplate.opsForValue().get(rateKey)
                .defaultIfEmpty("0")
                .flatMap(attempts -> {
                    int currentAttempts = Integer.parseInt(attempts);
                    if (currentAttempts >= MAX_ATTEMPTS) {
                        return Mono.error(new ApiException(
                            HttpStatus.TOO_MANY_REQUESTS,
                            "Too many attempts. Please try again later.",
                            "RATE_LIMIT_EXCEEDED"
                        ));
                    }
                    
                    return redisTemplate.opsForValue()
                            .increment(rateKey)
                            .then(redisTemplate.expire(rateKey, Duration.ofSeconds(BLOCK_DURATION_SECONDS)))
                            .then();
                });
    }

    public Mono<Void> resetRateLimit(String key, String action) {
        return redisTemplate.delete(RATE_LIMIT_PREFIX + action + ":" + key).then();
    }
}
