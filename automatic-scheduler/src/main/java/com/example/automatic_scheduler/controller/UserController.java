package com.example.automatic_scheduler.controller;

import com.example.automatic_scheduler.dto.UpdateUserRequest;
import com.example.automatic_scheduler.model.User;
import com.example.automatic_scheduler.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // 모든 사용자 조회
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    // 사용자 등록
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            User existingUser = userService.findByNameAndEmail(user.getName(), user.getEmail());
            if (existingUser != null) {
                return ResponseEntity.status(400).body("이미 등록된 사용자입니다.");
            }

            User savedUser = userService.save(user);
            return ResponseEntity.ok(savedUser); // 등록된 사용자 정보 반환
        } catch (Exception e) {
            return ResponseEntity.status(500).body("사용자 등록 중 오류 발생: " + e.getMessage());
        }
    }

    // 사용자 조회
    @GetMapping("/search")
    public ResponseEntity<?> getUserByNameAndEmail(@RequestParam("name") String name, @RequestParam("email") String email) {
        User user = userService.findByNameAndEmail(name, email);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(404).body("사용자를 찾을 수 없습니다.");
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest updateRequest) {
        try {
            User existingUser = userService.findById(id);
            if (existingUser != null) {
                // 수면 시간 및 취침 예정 시간 업데이트
                existingUser.setSleep_time(updateRequest.getSleep_time());
                existingUser.setBedtime(updateRequest.getBedtime());
                userService.save(existingUser);
                return ResponseEntity.ok(existingUser);
            } else {
                return ResponseEntity.status(404).body("사용자를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("사용자 업데이트 중 오류 발생: " + e.getMessage());
        }
    }
}
