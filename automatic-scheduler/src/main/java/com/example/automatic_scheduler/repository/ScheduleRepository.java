package com.example.automatic_scheduler.repository;

import com.example.automatic_scheduler.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByUserId(Long userId);
}