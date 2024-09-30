package com.colak.springtutorial.dto;

public record ApiErrorResponseDto(
        int errorCode,
        String description) {

}
