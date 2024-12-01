package com.example.automatic_scheduler.controller;

import com.example.automatic_scheduler.config.ApiException;
import com.example.automatic_scheduler.config.StatusCode;
import com.example.automatic_scheduler.model.Schedule;
import com.example.automatic_scheduler.service.ScheduleService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Setter
@Getter
@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @PostMapping
    public ResponseEntity<?> createSchedule(@RequestBody Schedule schedule) {
        try {
            Schedule createdSchedule = scheduleService.createSchedule(schedule);
            return ResponseEntity.ok(new ResponseMessage(true, createdSchedule.getScheduleId()));
        } catch (ApiException e) {
            // ApiException을 잡아 적절한 상태 코드와 메시지를 반환
            return ResponseEntity.status(e.getStatusCode().getHttpCode())
                    .body(e.getStatusCode().getErrorMessage());
        } catch (RuntimeException e) {
            // 일반적인 RuntimeException 처리
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}") // 추가된 부분: 사용자 ID로 일정을 조회
    public ResponseEntity<List<Schedule>> getUserSchedules(@PathVariable(name = "userId") Long userId) {
        List<Schedule> schedules = scheduleService.getUserSchedules(userId);
        System.out.println("Received User ID: " + userId);
        return ResponseEntity.ok(schedules);
    }

    @Setter
    @Getter
    public static class ResponseMessage {
        private boolean success;
        private Long scheduleId;

        public ResponseMessage(boolean success, Long scheduleId) {
            this.success = success;
            this.scheduleId = scheduleId;
        }
    }
}
