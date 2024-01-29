package com.colak.springsecurityjwtauthtutorial.entity;

import java.time.LocalDateTime;

public record LoginAttempt(String email,
                           boolean success,
                           LocalDateTime createdAt) {

}
