package com.example.automatic_scheduler.service;

import com.example.automatic_scheduler.config.ApiException;
import com.example.automatic_scheduler.config.ResponseSchedule;
import com.example.automatic_scheduler.config.StatusCode;
import com.example.automatic_scheduler.model.Schedule;
import com.example.automatic_scheduler.model.User;
import com.example.automatic_scheduler.repository.ScheduleRepository;
import com.example.automatic_scheduler.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private UserRepository userRepository;

    public Schedule createSchedule(Schedule schedule) {
        User user = userRepository.findById(schedule.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String batchType = schedule.getBatchType();

        try {
            if (batchType != null && batchType.equals("auto")) {
                autoSchedule(schedule, user);
            } else {
                manualSchedule(schedule);
            }
            return schedule;
        } catch (RuntimeException e) {
            throw new ApiException(StatusCode.CONFLICT_TIME_ERROR);
        }
    }

    private ResponseEntity<?> autoSchedule(Schedule schedule, User user) {
        int sleepTime = user.getSleep_time(); // 수면 시간 (시간 단위)
        LocalTime bedtime = LocalTime.parse(user.getBedtime()); // 취침 시간 (시간 단위)

        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime deadline = schedule.getDeadline(); // 이미 LocalDateTime으로 되어 있음

        // 마감 기한을 고려하여 가능한 시작 시간을 계산
        if (deadline.isAfter(currentTime.plusHours(sleepTime))) {
            LocalDateTime assignedStart = deadline.minusMinutes(schedule.getDuration());
            LocalDateTime assignedEnd = deadline;

            // assignedStart가 bedtime 이전인지 확인
            if (assignedStart.toLocalTime().isBefore(bedtime)) {
                schedule.setAssignedStart(assignedStart); // LocalDateTime으로 설정
                schedule.setAssignedEnd(assignedEnd); // LocalDateTime으로 설정
            } else {
                return ResponseEntity.badRequest().body("일정이 중복되었습니다.");
            }
        } else {
            return ResponseEntity.badRequest().body("시간이 부족합니다.");
        }

        scheduleRepository.save(schedule);
        return ResponseEntity.ok().body("일정 생성을 성공했습니다.");
    }

    private void manualSchedule(Schedule schedule) {
        scheduleRepository.save(schedule);
    }
}
