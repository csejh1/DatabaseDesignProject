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
        try {
            List<User> users = userService.findAll();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null); // 서버 오류
        }
    }

    // 사용자 등록
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            User savedUser = userService.save(user);
            return ResponseEntity.ok(savedUser); // 등록된 사용자 정보 반환
        } catch (Exception e) {
            return ResponseEntity.status(500).body("사용자 등록 중 오류 발생: " + e.getMessage());
        }
    }

    // 이름과 이메일로 사용자 조회
    @GetMapping("/search")
    public ResponseEntity<?> getUserByNameAndEmail(@RequestParam(value="name") String name, @RequestParam(value="email") String email) {
        try {
            User user = userService.findByNameAndEmail(name, email);
            if (user != null) {
                return ResponseEntity.ok(user); // 사용자 존재
            } else {
                return ResponseEntity.status(404).body("사용자를 찾을 수 없습니다."); // 사용자 없음
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("사용자 조회 중 오류 발생: " + e.getMessage());
        }
    }

    // 사용자 정보 업데이트
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable(name = "id") Long id, @RequestBody UpdateUserRequest updateRequest) {
        try {
            User existingUser = userService.findById(id);
            if (existingUser != null) {
                existingUser.setSleep_time(updateRequest.getSleep_time());
                existingUser.setBedtime(updateRequest.getBedtime());
                userService.save(existingUser); // 업데이트된 사용자 정보 저장
                return ResponseEntity.ok(existingUser); // 업데이트된 사용자 정보 반환
            } else {
                return ResponseEntity.status(404).body("사용자를 찾을 수 없습니다."); // 사용자 없음
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("사용자 업데이트 중 오류 발생: " + e.getMessage());
        }
    }
}
