package com.example.automatic_scheduler.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseSchedule {
    private boolean success;
    private Integer httpCode;
    private String errorMessage;

    public ResponseSchedule(final StatusCode statusCode) {
        this.success = statusCode.getSuceess();
        this.httpCode = statusCode.getHttpCode();
        this.errorMessage = statusCode.getErrorMessage();
    }

    public static ResponseSchedule of(final StatusCode statusCode) { return new ResponseSchedule(statusCode); }
}
