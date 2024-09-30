package com.colak.springtutorial.dto;

import com.colak.springtutorial.entity.LoginAttempt;

import java.time.LocalDateTime;

public record LoginAttemptResponseDto(
        LocalDateTime createdAt,
        boolean success) {

    public static LoginAttemptResponseDto convertToDTO(LoginAttempt loginAttempt) {
        return new LoginAttemptResponseDto(loginAttempt.createdAt(), loginAttempt.success());
    }
}
