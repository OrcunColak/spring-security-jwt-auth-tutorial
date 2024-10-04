package com.colak.springtutorial.service.loginattempt;

import com.colak.springtutorial.jpa.LoginAttempt;
import com.colak.springtutorial.repository.LoginAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final LoginAttemptRepository repository;

    @Transactional
    public void addLoginAttempt(String email, boolean success) {
        LoginAttempt loginAttempt = new LoginAttempt(email, success, LocalDateTime.now());
        repository.save(loginAttempt);
    }

    public List<LoginAttempt> findRecentLoginAttempts(String email) {
        return repository.findTop5ByEmailOrderByCreatedAtDesc(email);
    }
}
