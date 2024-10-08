package com.colak.springtutorial.controller;

import com.colak.springtutorial.dto.login.LoginRequestDto;
import com.colak.springtutorial.dto.login.LoginResponseDto;
import com.colak.springtutorial.service.accesstoken.AccessTokenService;
import com.colak.springtutorial.service.loginattempt.LoginAttemptService;
import com.colak.springtutorial.service.refreshtokenservice.RefreshTokenService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.colak.springtutorial.configuration.BearerAuthenticationConverter.COOKIE_NAME;

@RestController
@RequestMapping(path = "/api/auth")
@RequiredArgsConstructor
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final LoginAttemptService loginAttemptService;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;

    /**
     * The user does not have a Jwt Token in the request's Authorization header
     * Upon login the server needs to provide a JWT Token in return.
     */
    // http://localhost:8080/api/auth/login
    @PostMapping(value = "/login")
    public LoginResponseDto login(@Valid @RequestBody LoginRequestDto request,
                                                  HttpServletResponse response) {
        String email = request.email();
        try {
            // First authenticate the user
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));

            // Generate a new Jwt token
            String accessToken = accessTokenService.generateAccessToken(email);
            String refreshToken = refreshTokenService.createRefreshToken(email);

            loginAttemptService.addLoginAttempt(email, true);

            // Add the token to ResponseCookie
            // set cookie expiry for 30 minutes
            long cookieExpiry = 1800;

            ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, accessToken)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(cookieExpiry)
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            return new LoginResponseDto(email, accessToken, refreshToken);
        } catch (BadCredentialsException exception) {
            loginAttemptService.addLoginAttempt(email, false);
            throw exception;
        }
    }
}
