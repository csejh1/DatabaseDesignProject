package com.example.automatic_scheduler.controller;

import com.example.automatic_scheduler.config.ApiException;
import com.example.automatic_scheduler.config.ResponseSchedule;
import com.example.automatic_scheduler.config.StatusCode;
import com.example.automatic_scheduler.model.Schedule;
import com.example.automatic_scheduler.service.ScheduleService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        } catch (RuntimeException e) {
            //throw new ApiException(StatusCode.CONFLICT_TIME_ERROR);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Setter
    @Getter
    public static class ResponseMessage {
        // Getters and Setters
        private boolean success;
        private Long scheduleId;

        public ResponseMessage(boolean success, Long scheduleId) {
            this.success = success;
            this.scheduleId = scheduleId;
        }
    }
}
