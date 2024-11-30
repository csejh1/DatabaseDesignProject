package com.example.automatic_scheduler.service;

import com.example.automatic_scheduler.model.User;
import com.example.automatic_scheduler.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public User save(User user) {
        try {
            return userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("사용자 저장 중 오류 발생: " + e.getMessage());
        }
    }

    public User findByNameAndEmail(String name, String email) {
        return userRepository.findByNameAndEmail(name, email);
    }

    public User findById(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.orElse(null); // 사용자 존재하지 않을 경우 null 반환
    }
}
