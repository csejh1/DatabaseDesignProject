package com.example.automatic_scheduler.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ApiException extends RuntimeException {
    private final StatusCode statusCode;
    
    public ApiException(StatusCode statusCode, String message) {
        super(message); // 부모 클래스(RuntimeException)의 메시지를 설정
        this.statusCode = statusCode;
    }
}
