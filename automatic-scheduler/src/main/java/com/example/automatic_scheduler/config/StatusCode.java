package com.example.automatic_scheduler.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusCode {
    CONFLICT_TIME_ERROR(false,400, "일정이 중복되었습니다."),
    LEAK_TIME_ERROR(false, 400, "시간이 부족합니다.");

    private final Boolean suceess;
    private final Integer httpCode;
    private final String errorMessage;
}
