package com.colak.springsecurityjwtauthtutorial.dto;

public record ApiErrorResponseDto(
        int errorCode,
        String description) {

}
