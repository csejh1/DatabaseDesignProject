package com.example.automatic_scheduler.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateUserRequest {
    private Integer sleep_time; // sleepTime -> sleep_time으로 수정
    private String bedtime;
}
