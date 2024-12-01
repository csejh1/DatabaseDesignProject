package com.example.automatic_scheduler.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusCode {
    // 성공
    SUCCESS(true, 200, "요청이 성공적으로 처리되었습니다."),
    CREATED(true, 201, "일정이 성공적으로 생성되었습니다."),

    // 클라이언트 오류
    BAD_REQUEST(false, 402, "요청 본문이 비어 있거나 필수 필드가 누락되었습니다."),
    CONFLICT_TIME_ERROR(false, 403, "일정이 중복되었습니다."),
    LEAK_TIME_ERROR(false, 404, "시간이 부족합니다."),
    USER_NOT_FOUND(false, 401, "사용자를 찾을 수 없습니다."),
    INVALID_REQUEST(false, 405, "요청 본문이 비어 있거나 필수 필드가 누락되었습니다."),
    INVALID_INPUT(false,406 ,"" ),
    NOT_FOUND(false,407 ,"해당일정을 찾을 수 없습니다." ),
    SLEEP_TIME_ERROR(false,408 ,"수면시간을 포함합니다." ); // 수정된 부분


    private final Boolean suceess;
    private final Integer httpCode;
    private final String errorMessage;
}
