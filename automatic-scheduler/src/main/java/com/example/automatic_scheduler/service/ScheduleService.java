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

    private Schedule autoSchedule(Schedule schedule, User user) {
        int sleepTime = user.getSleep_time(); // 수면 시간 (시간 단위)
        LocalTime bedtime = LocalTime.parse(user.getBedtime()); // 취침 시간 (시간 단위)

        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime deadline = schedule.getDeadline(); // 이미 LocalDateTime으로 되어 있음

        // 마감 기한을 고려하여 가능한 시작 시간을 찾기
        LocalDateTime availableStart = findAvailableTimeSlot(deadline, schedule.getDuration(), bedtime, user,schedule.getPriority());

        if (availableStart == null) {
            throw new ApiException(StatusCode.CONFLICT_TIME_ERROR); // 가능한 시간이 없을 경우 예외 발생
        }

        LocalDateTime assignedEnd = availableStart.plusMinutes(schedule.getDuration());
        schedule.setAssignedStart(availableStart); // LocalDateTime으로 설정
        schedule.setAssignedEnd(assignedEnd); // LocalDateTime으로 설정

        scheduleRepository.save(schedule);
        return schedule; // 생성된 일정을 반환
    }



    private Schedule manualSchedule(Schedule schedule) {
        scheduleRepository.save(schedule);
        return schedule; // 생성된 일정을 반환
    }

    private LocalDateTime findAvailableTimeSlot(LocalDateTime deadline, int duration, LocalTime bedtime, User user, int priority) {
        LocalDateTime now = LocalDateTime.now();

        // Null 검증
        if (deadline == null) {
            throw new ApiException(StatusCode.INVALID_INPUT, "Deadline cannot be null.");
        }
        if (bedtime == null) {
            throw new ApiException(StatusCode.INVALID_INPUT, "Bedtime cannot be null.");
        }

        // Debug 로그
        System.out.println("Debug: Deadline = " + deadline);
        System.out.println("Debug: Bedtime = " + bedtime);
        System.out.println("Debug: Priority = " + priority);

        LocalTime wakeupTime = bedtime.plusHours(user.getSleep_time()); // 기상 시간 계산
        LocalDateTime assignedStart;
        LocalDateTime assignedEnd;

        if (priority == 1) {
            // 우선순위 1: 현재 시간 기준으로 탐색
            LocalDateTime bedtimeStart = LocalDateTime.of(now.toLocalDate(), bedtime); // 오늘 취침 시간
            LocalDateTime nextWakeupTime = bedtimeStart.plusDays(1).toLocalDate().atTime(wakeupTime); // 다음날 기상 시간

            // 현재 시간이 취침~기상 시간 범위에 있는지 확인
            if (now.toLocalTime().isAfter(bedtime) || now.toLocalTime().isBefore(wakeupTime)) {
                System.out.println("Current time is within sleep hours. Adjusting to next wakeup time.");
                assignedStart = nextWakeupTime;
            } else {
                // 현재 시간이 수면 시간이 아니면, 다음 30분 블록으로 조정
                assignedStart = now.plusMinutes(30 - (now.getMinute() % 30));
            }
        } else if (priority == 2) {
            // 우선순위 2: 마감 기한 기준 전날 기상 시간부터 탐색
            LocalDateTime previousWakeupTime = deadline.minusDays(1).toLocalDate().atTime(wakeupTime);
            System.out.println("Priority 2: Starting from previous wakeup time: " + previousWakeupTime);
            assignedStart = previousWakeupTime;
        } else {
            throw new ApiException(StatusCode.INVALID_INPUT, "Invalid priority value: " + priority);
        }

        assignedEnd = assignedStart.plusMinutes(duration);
        System.out.println("Debug: Initial Assigned Start = " + assignedStart);
        System.out.println("Debug: Initial Assigned End = " + assignedEnd);

        // 가능한 시간대 찾기
        while (assignedStart.isBefore(deadline)) {
            // 수면시간 체크
            if (assignedStart.toLocalTime().isAfter(bedtime) && assignedStart.toLocalTime().isBefore(wakeupTime)) {
                System.out.println("Skipping assigned start time within sleep hours: " + assignedStart);
                assignedStart = assignedStart.toLocalDate().atTime(wakeupTime).plusDays(1); // 다음날 기상 시간으로 건너뜀
                assignedEnd = assignedStart.plusMinutes(duration);
                continue;
            }

            // 스케줄 충돌 체크
            if (!checkScheduleConflict(assignedStart, assignedEnd, user.getUser_id())) {
                System.out.println("Available slot found: " + assignedStart);
                return assignedStart;
            }

            // 다음 30분 블록으로 이동
            assignedStart = assignedStart.plusMinutes(30);
            assignedEnd = assignedStart.plusMinutes(duration);
        }

        System.out.println("No available time slot found.");
        return null; // 가능한 슬롯이 없을 경우 null 반환
    }



    private boolean checkScheduleConflict(LocalDateTime assignedStart, LocalDateTime assignedEnd, Long userId) {
        // 현재 사용자의 기존 일정 조회
        List<Schedule> existingSchedules = scheduleRepository.findByUserId(userId);

        // 디버깅 로그: 불러온 일정 출력
        System.out.println("Checking schedule conflicts for userId: " + userId);
        System.out.println("Assigned slot: " + assignedStart + " to " + assignedEnd);
        System.out.println("Existing schedules:");

        for (Schedule existingSchedule : existingSchedules) {
            LocalDateTime existingStart = existingSchedule.getAssignedStart();
            LocalDateTime existingEnd = existingSchedule.getAssignedEnd();
            System.out.println("- Schedule ID: " + existingSchedule.getScheduleId()
                    + ", Start: " + existingStart
                    + ", End: " + existingEnd);

            // 새 일정의 시작 시간과 종료 시간이 기존 일정과 겹치는지 확인
            if (assignedStart.isBefore(existingEnd) && assignedEnd.isAfter(existingStart)) {
                System.out.println("Conflict found with Schedule ID: " + existingSchedule.getScheduleId());
                return true; // 일정이 겹치는 경우
            }
        }

        // 디버깅 로그: 충돌 없음
        System.out.println("No conflicts found.");
        return false; // 겹치는 일정이 없는 경우
    }

}
