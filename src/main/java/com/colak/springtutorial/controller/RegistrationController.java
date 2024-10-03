package com.colak.springtutorial.controller;

import com.colak.springtutorial.dto.signup.SignupRequestDto;
import com.colak.springtutorial.service.registration.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/auth")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequestDto signupRequest) {
        registrationService.signup(signupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
