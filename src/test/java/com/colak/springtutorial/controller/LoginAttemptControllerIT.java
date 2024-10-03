package com.colak.springtutorial.controller;

import com.colak.springtutorial.dto.login.LoginRequestDto;
import com.colak.springtutorial.dto.login.LoginResponseDto;
import com.colak.springtutorial.dto.loginattempt.LoginAttemptResponseDto;
import com.colak.springtutorial.dto.signup.SignupRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.ArrayList;
import java.util.List;

import static com.colak.springtutorial.controller.LoginControllerIT.LOGIN_URL;
import static com.colak.springtutorial.controller.RegistrationControllerIT.SIGNUP_URL;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class LoginAttemptControllerIT {

    static final String LOGIN_ATTEMPTS_URL = "/api/auth/loginAttempts";

    @SuppressWarnings("unused")
    @Autowired
    private WebTestClient webTestClient;

    //  Test loginAttempts endpoint
    @Test
    void shouldReturnLoginAttempts_WhenUserIsRegistered() {
        SignupRequestDto signupRequest = new SignupRequestDto(
                "william@gmail.com",
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

        LoginRequestDto loginRequest = new LoginRequestDto("william@gmail.com","123456");
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

        List<LoginAttemptResponseDto> loginAttemptsResponse = webTestClient
                .get().uri(LOGIN_ATTEMPTS_URL)
                .header("Authorization", "Bearer " + loginResponse.accessToken())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(LoginAttemptResponseDto.class)
                .returnResult()
                .getResponseBody();

        assertThat(loginAttemptsResponse).isNotNull().isNotEmpty();

        LoginAttemptResponseDto firstLoginAttempt = loginAttemptsResponse.getFirst();
        assertThat(firstLoginAttempt).isNotNull();
        assertThat(firstLoginAttempt.createdAt()).isNotNull();
        assertThat(firstLoginAttempt.success()).isTrue();
    }

    @Test
    void shouldReturnUnauthorized_withNoAuthorizationHeader() {
        webTestClient
                .get().uri(LOGIN_ATTEMPTS_URL)
//        .header("Authorization", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtaW5hQGdtYWlsLmNvbSIsImlhdCI6MTcwMjMwMjE0MCwiZXhwIjoxNzAyMzA1NzQwfQ.P0dlSC385lgtyRAr9Ako_hocxa2CvBV_hPAj-RjNtTw")
                .exchange()
                .expectStatus()
                .isUnauthorized();

//    String errorResponse = webTestClient
//        .get().uri(LOGIN_ATTEMPTS_URL)
////        .header("Authorization", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtaW5hQGdtYWlsLmNvbSIsImlhdCI6MTcwMjMwMjE0MCwiZXhwIjoxNzAyMzA1NzQwfQ.P0dlSC385lgtyRAr9Ako_hocxa2CvBV_hPAj-RjNtTw")
//        .exchange()
//        .expectStatus()
//        .isForbidden()
//        .expectBody(String.class)
//        .returnResult()
//        .getResponseBody();
//
//    assertThat(errorResponse).isNotNull();
//    assertThat(errorResponse).isEqualTo("{\"errorCode\":401,\"description\":\"Access denied: Authorization header is required.\"}");
    }

    @Test
    void shouldReturnUnauthorized_withEmptyAuthorizationHeader() {
        // Send empty Authorization
        // isUnauthorized : client making the request lacks valid authentication credentials
        // isForbidden : client has authenticated itself, but it does not have the necessary permissions
        String errorResponse = webTestClient
                .get().uri(LOGIN_ATTEMPTS_URL)
                .header("Authorization", " ")
                .exchange()
                .expectStatus()
                .isUnauthorized()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse).isEqualTo("{\"errorCode\":401,\"description\":\"Access denied: Authorization header is required.\"}");
    }
}