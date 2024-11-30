package com.example.automatic_scheduler.repository;

import com.example.automatic_scheduler.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByNameAndEmail(String name, String email);
}
