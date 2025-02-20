package com.example.jwtdemo.service;

import com.example.jwtdemo.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.Duration;

@Service
public class TokenService {

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;

    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    private static final String REFRESH_TOKEN_PREFIX = "token:refresh:";
    private static final String RESET_TOKEN_PREFIX = "token:reset:";
    private static final Duration DEFAULT_BLACKLIST_DURATION = Duration.ofDays(1); // Blacklist tokens for 1 day by default

    public Mono<Boolean> isTokenBlacklisted(String token) {
        return redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + token);
    }

    public Mono<Void> blacklistToken(String token) {
        return blacklistToken(token, DEFAULT_BLACKLIST_DURATION);
    }

    public Mono<Void> blacklistToken(String token, Duration duration) {
        return redisTemplate.opsForValue()
                .set(TOKEN_BLACKLIST_PREFIX + token, "", duration)
                .then();
    }

    public Mono<String> getRefreshToken(String userId) {
        return redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId)
                .switchIfEmpty(Mono.error(new ApiException(
                    HttpStatus.UNAUTHORIZED, 
                    "Refresh token not found", 
                    "INVALID_REFRESH_TOKEN"
                )));
    }

    public Mono<Void> saveRefreshToken(String userId, String refreshToken, Duration duration) {
        return redisTemplate.opsForValue()
                .set(REFRESH_TOKEN_PREFIX + userId, refreshToken, duration)
                .then();
    }

    public Mono<Void> removeRefreshToken(String userId) {
        return redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId).then();
    }

    public Mono<String> getUserIdFromResetToken(String token) {
        return redisTemplate.opsForValue().get(RESET_TOKEN_PREFIX + token)
                .switchIfEmpty(Mono.error(new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Reset token not found or expired",
                    "INVALID_RESET_TOKEN"
                )));
    }

    public Mono<Void> saveResetToken(String token, String userId, Duration duration) {
        return redisTemplate.opsForValue()
                .set(RESET_TOKEN_PREFIX + token, userId, duration)
                .then();
    }

    public Mono<Void> removeResetToken(String token) {
        return redisTemplate.delete(RESET_TOKEN_PREFIX + token).then();
    }
}
