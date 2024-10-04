package com.colak.springtutorial.controller;

import com.colak.springtutorial.dto.ApiErrorResponseDto;
import com.colak.springtutorial.dto.login.LoginRequestDto;
import com.colak.springtutorial.dto.login.LoginResponseDto;
import com.colak.springtutorial.dto.signup.SignupRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.ArrayList;

import static com.colak.springtutorial.controller.RegistrationControllerIT.SIGNUP_URL;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class LoginControllerIT {

    static final String LOGIN_URL = "/api/auth/login";

    @SuppressWarnings("unused")
    @Autowired
    private WebTestClient webTestClient;

    //  Test login endpoint
    @Test
    void shouldReturnJWTToken_WhenUserIsRegistered() {
        SignupRequestDto signupRequest = new SignupRequestDto(
                "nick@gmail.com",
                "123456",
                new ArrayList<>()
        );

        webTestClient
                .post().uri(SIGNUP_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(signupRequest)
                .exchange()
                .expectStatus()
                .isCreated();

        LoginRequestDto loginRequest = new LoginRequestDto("nick@gmail.com","123456");
        LoginResponseDto loginResponse = webTestClient
                .post().uri(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(LoginResponseDto.class)
                .returnResult()
                .getResponseBody();

        assertThat(loginResponse).isNotNull();
        assertThat(loginResponse.accessToken()).isNotBlank();
        assertThat(loginResponse.refreshToken()).isNotBlank();
    }

    @Test
    void shouldReturnUnauthorized_WhenBadCredential() {
        // Sign up
        SignupRequestDto signupRequest = new SignupRequestDto(
                "john@gmail.com",
                "123456",
                new ArrayList<>()
        );
        webTestClient
                .post().uri(SIGNUP_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(signupRequest)
                .exchange()
                .expectStatus()
                .isCreated();

        // Password is different
        LoginRequestDto loginRequestWithWrongPassword = new LoginRequestDto("john@gmail.com","12345678910");

        ApiErrorResponseDto errorResponse = webTestClient
                .post().uri(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequestWithWrongPassword)
                .exchange()
                .expectStatus()
                .isUnauthorized()
                .expectBody(ApiErrorResponseDto.class)
                .returnResult()
                .getResponseBody();

        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.errorCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(errorResponse.description()).isEqualTo("Invalid username or password");
    }

    @Test
    void shouldReturnUnauthorized_WhenUserNotRegistered() {
        LoginRequestDto loginRequestForRegisteredUser = new LoginRequestDto("sara@gmail.com","123456");

        ApiErrorResponseDto errorResponse = webTestClient
                .post().uri(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequestForRegisteredUser)
                .exchange()
                .expectStatus()
                .isUnauthorized()
                .expectBody(ApiErrorResponseDto.class)
                .returnResult()
                .getResponseBody();

        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.errorCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(errorResponse.description()).isEqualTo("User does not exist, email: sara@gmail.com");
    }
}