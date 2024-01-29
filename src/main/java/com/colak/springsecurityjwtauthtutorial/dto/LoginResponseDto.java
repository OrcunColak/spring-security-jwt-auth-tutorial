package com.colak.springsecurityjwtauthtutorial.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponseDto(
        @Schema(description = "email")
        String email,
        @Schema(description = "JWT token")
        String token) {

}
