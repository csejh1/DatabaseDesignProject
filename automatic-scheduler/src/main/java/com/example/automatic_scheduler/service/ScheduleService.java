package com.example.automatic_scheduler.service;

import com.example.automatic_scheduler.config.ApiException;
import com.example.automatic_scheduler.config.StatusCode;
import com.example.automatic_scheduler.model.Schedule;
import com.example.automatic_scheduler.model.User;
import com.example.automatic_scheduler.repository.ScheduleRepository;
import com.example.automatic_scheduler.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private UserRepository userRepository;

    public Schedule createSchedule(Schedule schedule) {
        User user = userRepository.findById(schedule.getUserId())
                .orElseThrow(() -> new ApiException(StatusCode.USER_NOT_FOUND));

        String batchType = schedule.getBatchType();

        if (batchType != null && batchType.equals("auto")) {
            return autoSchedule(schedule, user);
        } else {
            return manualSchedule(schedule);
        }
    }

    public List<Schedule> getUserSchedules(Long userId) {
        return scheduleRepository.findByUserId(userId);
    }

    public Schedule updateSchedule(Long scheduleId, Schedule schedule) {
        // 기존 일정 조회
        Schedule existingSchedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ApiException(StatusCode.NOT_FOUND));

        // 사용자 정보 조회
        User user = userRepository.findById(schedule.getUserId())
                .orElseThrow(() -> new ApiException(StatusCode.USER_NOT_FOUND));

        // 시간 관련 검증 (수면 시간 및 충돌 확인)
        int sleepTime = user.getSleep_time(); // 수면 시간
        LocalTime bedtime = LocalTime.parse(user.getBedtime()); // 취침 시간
        LocalDateTime assignedStart = schedule.getAssignedStart();
        LocalDateTime assignedEnd = schedule.getAssignedEnd();

        // 수면 시간 검증
        if (isOverlappingWithSleepTime(assignedStart, assignedEnd, sleepTime, bedtime)) {
            throw new ApiException(StatusCode.SLEEP_TIME_ERROR);
        }

        // 기존 일정과의 충돌 여부 확인
        if (checkScheduleConflict(assignedStart, assignedEnd, schedule.getUserId())) {
            throw new ApiException(StatusCode.CONFLICT_TIME_ERROR);
        }

        // 검증 통과 후 업데이트
        existingSchedule.setPriority(schedule.getPriority());
        existingSchedule.setStatus(schedule.getStatus());
        existingSchedule.setAssignedStart(schedule.getAssignedStart());
        existingSchedule.setAssignedEnd(schedule.getAssignedEnd());

        return scheduleRepository.save(existingSchedule);
    }



    public void deleteSchedule(Long scheduleId) {
        Schedule existingSchedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ApiException(StatusCode.NOT_FOUND, "Schedule not found"));
        scheduleRepository.delete(existingSchedule);
    }

    private Schedule autoSchedule(Schedule schedule, User user) {
        int sleepTime = user.getSleep_time();
        LocalTime bedtime = LocalTime.parse(user.getBedtime());

        LocalDateTime deadline = schedule.getDeadline();
        LocalDateTime availableStart = findAvailableTimeSlot(deadline, schedule.getDuration(), bedtime, user, schedule.getPriority());

        if (availableStart == null) {
            throw new ApiException(StatusCode.CONFLICT_TIME_ERROR);
        }

        LocalDateTime assignedEnd = availableStart.plusMinutes(schedule.getDuration());
        schedule.setAssignedStart(availableStart);
        schedule.setAssignedEnd(assignedEnd);

        scheduleRepository.save(schedule);
        return schedule;
    }

    private Schedule manualSchedule(Schedule schedule) {
        User user = userRepository.findById(schedule.getUserId())
                .orElseThrow(() -> new ApiException(StatusCode.USER_NOT_FOUND));

        int sleepTime = user.getSleep_time();
        LocalTime bedtime = LocalTime.parse(user.getBedtime());

        LocalDateTime assignedStart = schedule.getAssignedStart();
        LocalDateTime assignedEnd = schedule.getAssignedEnd();

        // Debug 로그
        System.out.println("Manual Schedule: Checking conflicts");
        System.out.println("Assigned Start: " + assignedStart);
        System.out.println("Assigned End: " + assignedEnd);
        System.out.println("Bedtime: " + bedtime + ", Sleep Time: " + sleepTime);

        if (isOverlappingWithSleepTime(assignedStart, assignedEnd, sleepTime, bedtime)) {
            throw new ApiException(StatusCode.SLEEP_TIME_ERROR);
        }

        if (checkScheduleConflict(assignedStart, assignedEnd, schedule.getUserId())) {
            throw new ApiException(StatusCode.CONFLICT_TIME_ERROR);
        }

        scheduleRepository.save(schedule);
        return schedule;
    }

    private boolean isOverlappingWithSleepTime(LocalDateTime start, LocalDateTime end, int sleepTime, LocalTime bedtime) {
        // 취침 시간: 오늘의 bedtime
        LocalDateTime bedtimeStart = LocalDateTime.of(start.toLocalDate(), bedtime);

        // 기상 시간: bedtime + sleepTime
        LocalDateTime wakeupTime = bedtimeStart.plusHours(sleepTime);

        // 만약 bedtime이 현재 시간보다 이전이고 wakeup이 다음날로 넘어가는 경우를 자동 처리
        if (bedtime.isBefore(LocalTime.of(12, 0)) && wakeupTime.isBefore(bedtimeStart)) {
            wakeupTime = wakeupTime.plusDays(1); // 다음날로 보정
        }

        // Debug 로그
        System.out.println("Checking sleep time overlap:");
        System.out.println("Sleep Time Range: " + bedtimeStart + " to " + wakeupTime);
        System.out.println("Schedule: " + start + " to " + end);

        // 겹치는지 확인
        boolean overlap = start.isBefore(wakeupTime) && end.isAfter(bedtimeStart);
        if (overlap) {
            System.out.println("Conflict with sleep time.");
        } else {
            System.out.println("No conflict with sleep time.");
        }

        return overlap;
    }



    private LocalDateTime findAvailableTimeSlot(LocalDateTime deadline, int duration, LocalTime bedtime, User user, int priority) {
        LocalDateTime now = LocalDateTime.now();

        if (deadline == null || bedtime == null) {
            throw new ApiException(StatusCode.INVALID_INPUT, "Deadline or bedtime cannot be null.");
        }

        System.out.println("Debug: Deadline = " + deadline);
        System.out.println("Debug: Bedtime = " + bedtime);
        System.out.println("Debug: Priority = " + priority);

        LocalTime wakeupTime = bedtime.plusHours(user.getSleep_time());
        LocalDateTime assignedStart;

        if (priority == 1) {
            LocalDateTime bedtimeStart = LocalDateTime.of(now.toLocalDate(), bedtime);
            LocalDateTime nextWakeupTime = bedtimeStart.plusDays(1).toLocalDate().atTime(wakeupTime);

            if (now.toLocalTime().isAfter(bedtime) || now.toLocalTime().isBefore(wakeupTime)) {
                assignedStart = nextWakeupTime;
            } else {
                assignedStart = now.plusMinutes(30 - (now.getMinute() % 30));
            }
        } else if (priority == 2) {
            LocalDateTime previousWakeupTime = deadline.minusDays(1).toLocalDate().atTime(wakeupTime);
            assignedStart = previousWakeupTime;
        } else {
            throw new ApiException(StatusCode.INVALID_INPUT, "Invalid priority value: " + priority);
        }

        LocalDateTime assignedEnd = assignedStart.plusMinutes(duration);
        System.out.println("Initial Assigned Start: " + assignedStart);
        System.out.println("Initial Assigned End: " + assignedEnd);

        while (assignedStart.isBefore(deadline)) {
            if (assignedStart.toLocalTime().isAfter(bedtime) && assignedStart.toLocalTime().isBefore(wakeupTime)) {
                assignedStart = assignedStart.toLocalDate().atTime(wakeupTime).plusDays(1);
                assignedEnd = assignedStart.plusMinutes(duration);
                continue;
            }

            if (!checkScheduleConflict(assignedStart, assignedEnd, user.getUser_id())) {
                System.out.println("Available slot found: " + assignedStart);
                return assignedStart;
            }

            assignedStart = assignedStart.plusMinutes(30);
            assignedEnd = assignedStart.plusMinutes(duration);
        }

        System.out.println("No available time slot found.");
        return null;
    }

    private boolean checkScheduleConflict(LocalDateTime assignedStart, LocalDateTime assignedEnd, Long userId) {
        List<Schedule> existingSchedules = scheduleRepository.findByUserId(userId);

        System.out.println("Checking schedule conflicts for userId: " + userId);
        System.out.println("Assigned slot: " + assignedStart + " to " + assignedEnd);
        System.out.println("Existing schedules:");

        for (Schedule existingSchedule : existingSchedules) {

            LocalDateTime existingStart = existingSchedule.getAssignedStart();
            LocalDateTime existingEnd = existingSchedule.getAssignedEnd();
            System.out.println("- Schedule ID: " + existingSchedule.getScheduleId()
                    + ", Start: " + existingStart
                    + ", End: " + existingEnd);

            if (assignedStart.isBefore(existingEnd) && assignedEnd.isAfter(existingStart)) {
                System.out.println("Conflict found with Schedule ID: " + existingSchedule.getScheduleId());
                return true;
            }
        }

        System.out.println("No conflicts found.");
        return false;
    }
}
