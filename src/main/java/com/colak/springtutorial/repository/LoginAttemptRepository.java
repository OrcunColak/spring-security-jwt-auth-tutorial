package com.colak.springtutorial.repository;

import com.colak.springtutorial.jpa.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    // Custom method to find the most recent login attempts by email, sorted by createdAt
    List<LoginAttempt> findTop5ByEmailOrderByCreatedAtDesc(String email);
}

