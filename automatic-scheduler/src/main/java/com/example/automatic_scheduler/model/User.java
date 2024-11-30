package com.example.automatic_scheduler.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "User")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long user_id; // user_Id -> user_id로 수정

    private String name;

    private String email;

    private Integer sleep_time; // sleep_time -> sleep_time으로 유지

    private String bedtime; // LocalDateTime으로 변경
}
