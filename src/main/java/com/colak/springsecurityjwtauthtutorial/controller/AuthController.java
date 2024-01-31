package com.colak.springsecurityjwtauthtutorial.controller;

import com.colak.springsecurityjwtauthtutorial.dto.LoginAttemptResponseDto;
import com.colak.springsecurityjwtauthtutorial.dto.LoginRequestDto;
import com.colak.springsecurityjwtauthtutorial.dto.LoginResponseDto;
import com.colak.springsecurityjwtauthtutorial.dto.SignupRequestDto;
import com.colak.springsecurityjwtauthtutorial.entity.LoginAttempt;
import com.colak.springsecurityjwtauthtutorial.helper.JwtHelper;
import com.colak.springsecurityjwtauthtutorial.service.LoginService;
import com.colak.springsecurityjwtauthtutorial.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final LoginService loginService;

    // http://localhost:8080/api/auth/signup
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequestDto signupRequestDto) {
        userService.signup(signupRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // http://localhost:8080/api/auth/login
    @PostMapping(value = "/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (BadCredentialsException e) {
            loginService.addLoginAttempt(request.email(), false);
            throw e;
        }

        String token = JwtHelper.generateToken(request.email());
        loginService.addLoginAttempt(request.email(), true);
        return ResponseEntity.ok(new LoginResponseDto(request.email(), token));
    }


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
