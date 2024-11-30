package com.example.automatic_scheduler.config;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ApiException.class)
    protected ResponseEntity<Object> apiException(ApiException e) {
        ResponseSchedule responseSchedule = ResponseSchedule.of(e.getStatusCode());
        return new ResponseEntity<>(responseSchedule, HttpStatusCode.valueOf(responseSchedule.getHttpCode()));
    }
}
