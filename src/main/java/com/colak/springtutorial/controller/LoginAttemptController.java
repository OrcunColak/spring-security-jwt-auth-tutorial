package com.colak.springtutorial.controller;

import com.colak.springtutorial.dto.loginattempt.LoginAttemptResponseDto;
import com.colak.springtutorial.helper.JwtHelper;
import com.colak.springtutorial.jpa.LoginAttempt;
import com.colak.springtutorial.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/api/auth")
@RequiredArgsConstructor
public class LoginAttemptController {

    private final LoginService loginService;

    // http://localhost:8080/api/auth/loginAttempts
    @GetMapping(value = "/loginAttempts")
    public ResponseEntity<List<LoginAttemptResponseDto>> loginAttempts(@RequestHeader("Authorization") String token) {
        String email = JwtHelper.extractUsername(token.replace("Bearer ", ""));
        List<LoginAttempt> loginAttempts = loginService.findRecentLoginAttempts(email);
        return ResponseEntity.ok(convertToDTOs(loginAttempts));
    }

    private List<LoginAttemptResponseDto> convertToDTOs(List<LoginAttempt> loginAttempts) {
        return loginAttempts.stream()
                .map(LoginAttemptResponseDto::convertToDTO)
                .toList();
    }
}