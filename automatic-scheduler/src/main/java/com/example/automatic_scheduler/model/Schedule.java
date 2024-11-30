package com.example.automatic_scheduler.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Setter
@Getter
@Entity
@Table(name = "schedules")
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleId;

    private Long userId;
    private String title;
    private String description;
    private int priority;
    private String status;

    @JsonProperty("batch_type")
    @Column(name = "batch_type")
    private String batchType;

    private Integer duration; // 분 단위

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime deadline; // LocalDateTime으로 변경


    @JsonProperty("assigned_start")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    @Column(name = "assigned_start")
    private LocalDateTime assignedStart; // LocalDateTime으로 변경

    @JsonProperty("assigned_end")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    @Column(name = "assigned_end")
    private LocalDateTime assignedEnd; // LocalDateTime으로 변경

    public String toString() {
        return "Schedule{" +
                "scheduleId=" + scheduleId +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", priority=" + priority +
                ", status='" + status + '\'' +
                ", batchType='" + batchType + '\'' +
                ", duration=" + duration +
                ", deadline=" + deadline +
                ", assignedStart=" + assignedStart +
                ", assignedEnd=" + assignedEnd +
                '}';
    }
}
