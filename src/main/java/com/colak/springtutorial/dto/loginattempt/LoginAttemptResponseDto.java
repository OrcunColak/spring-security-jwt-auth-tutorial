package com.colak.springtutorial.dto.loginattempt;


import com.colak.springtutorial.jpa.LoginAttempt;

import java.time.LocalDateTime;

public record LoginAttemptResponseDto(
        LocalDateTime createdAt,
        boolean success) {

    public static LoginAttemptResponseDto convertToDTO(LoginAttempt loginAttempt) {
        return new LoginAttemptResponseDto(loginAttempt.getCreatedAt(), loginAttempt.isSuccess());
    }
}
