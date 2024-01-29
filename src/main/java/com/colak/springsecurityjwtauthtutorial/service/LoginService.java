package com.colak.springsecurityjwtauthtutorial.service;

import com.colak.springsecurityjwtauthtutorial.entity.LoginAttempt;
import com.colak.springsecurityjwtauthtutorial.repository.LoginAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final LoginAttemptRepository repository;

    @Transactional
    public void addLoginAttempt(String email, boolean success) {
        LoginAttempt loginAttempt = new LoginAttempt(email, success, LocalDateTime.now());
        repository.add(loginAttempt);
    }

    public List<LoginAttempt> findRecentLoginAttempts(String email) {
        return repository.findRecent(email);
    }
}
