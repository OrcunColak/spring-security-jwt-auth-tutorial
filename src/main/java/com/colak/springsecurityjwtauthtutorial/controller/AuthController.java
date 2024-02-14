package com.colak.springsecurityjwtauthtutorial.controller;

import com.colak.springsecurityjwtauthtutorial.configuration.JwtAuthFilter;
import com.colak.springsecurityjwtauthtutorial.dto.LoginAttemptResponseDto;
import com.colak.springsecurityjwtauthtutorial.dto.LoginRequestDto;
import com.colak.springsecurityjwtauthtutorial.dto.LoginResponseDto;
import com.colak.springsecurityjwtauthtutorial.dto.SignupRequestDto;
import com.colak.springsecurityjwtauthtutorial.entity.LoginAttempt;
import com.colak.springsecurityjwtauthtutorial.helper.JwtHelper;
import com.colak.springsecurityjwtauthtutorial.service.LoginService;
import com.colak.springsecurityjwtauthtutorial.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
    private final UserService userService;
    private final LoginService loginService;

    // http://localhost:8080/api/auth/signup
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequestDto signupRequestDto) {
        userService.signup(signupRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

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

    @Operation(summary = "Get quote if logged in")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logged in successfully.", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403", description = "Not Logged in successfully.", content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping(value = "/quote")
    public String quote() {
        return "quote";
    }

    private List<LoginAttemptResponseDto> convertToDTOs(List<LoginAttempt> loginAttempts) {
        return loginAttempts.stream()
                .map(LoginAttemptResponseDto::convertToDTO)
                .toList();
    }
}
