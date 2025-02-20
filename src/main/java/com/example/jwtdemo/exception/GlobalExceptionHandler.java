package com.example.jwtdemo.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleApiException(ApiException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", ex.getStatus().value());
        response.put("error", ex.getStatus().getReasonPhrase());
        response.put("message", ex.getMessage());
        response.put("errorCode", ex.getErrorCode());

        return Mono.just(ResponseEntity.status(ex.getStatus()).body(response));
    }
}
