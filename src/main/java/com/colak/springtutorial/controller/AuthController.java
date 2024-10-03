package com.colak.springtutorial.controller;

import com.colak.springtutorial.configuration.JwtAuthFilter;
import com.colak.springtutorial.dto.login.LoginRequestDto;
import com.colak.springtutorial.dto.login.LoginResponseDto;
import com.colak.springtutorial.dto.loginattempt.LoginAttemptResponseDto;
import com.colak.springtutorial.entity.LoginAttempt;
import com.colak.springtutorial.helper.JwtHelper;
import com.colak.springtutorial.service.LoginService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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
    private final LoginService loginService;

    /**
     * The user does not have a Jwt Token in the request's Authorization header
     * Upon login the server needs to provide a JWT Token in return.
     */
    // http://localhost:8080/api/auth/login
    @PostMapping(value = "/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request,
                                                  HttpServletResponse response) {
        try {
            // First authenticate the user
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));

            // Generate a new Jwt token
            String accessToken = JwtHelper.generateToken(request.email());
            loginService.addLoginAttempt(request.email(), true);

            // Add the token to ResponseCookie
            // set cookie expiry for 30 minutes
            long cookieExpiry = 1800;

            ResponseCookie cookie = ResponseCookie.from(JwtAuthFilter.COOKIE_NAME, accessToken)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(cookieExpiry)
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            LoginResponseDto body = new LoginResponseDto(request.email(), accessToken);
            return ResponseEntity.ok(body);

        } catch (BadCredentialsException exception) {
            loginService.addLoginAttempt(request.email(), false);
            throw exception;
        }
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
