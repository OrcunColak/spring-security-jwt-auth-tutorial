package com.colak.springtutorial.controller;

import com.colak.springtutorial.dto.ApiErrorResponseDto;
import com.colak.springtutorial.exception.DuplicateException;
import com.colak.springtutorial.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponseDto> handleNotFoundException(NotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponseDto(HttpStatus.NOT_FOUND.value(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseDto> handleRequestNotValidException(MethodArgumentNotValidException exception) {

        List<String> errors = new ArrayList<>();
        exception.getBindingResult()
                .getFieldErrors().forEach(error -> errors.add(error.getField() + ": " + error.getDefaultMessage()));
        exception.getBindingResult()
                .getGlobalErrors() //Global errors are not associated with a specific field but are related to the entire object being validated.
                .forEach(error -> errors.add(error.getObjectName() + ": " + error.getDefaultMessage()));

        String message = "Validation of request failed: %s".formatted(String.join(", ", errors));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiErrorResponseDto(HttpStatus.BAD_REQUEST.value(), message));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponseDto> handleBadCredentialsException() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiErrorResponseDto(HttpStatus.UNAUTHORIZED.value(), "Invalid username or password"));
    }

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<ApiErrorResponseDto> handleDuplicateException(DuplicateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiErrorResponseDto(HttpStatus.CONFLICT.value(), e.getMessage()));
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<ApiErrorResponseDto> handleInternalAuthenticationServiceException(InternalAuthenticationServiceException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiErrorResponseDto(HttpStatus.UNAUTHORIZED.value(), e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponseDto> handleUnknownException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiErrorResponseDto(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
    }

}
