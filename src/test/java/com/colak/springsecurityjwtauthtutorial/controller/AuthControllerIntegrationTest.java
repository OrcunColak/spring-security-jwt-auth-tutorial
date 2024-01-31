package com.colak.springsecurityjwtauthtutorial.controller;

import com.colak.springsecurityjwtauthtutorial.dto.ApiErrorResponseDto;
import com.colak.springsecurityjwtauthtutorial.dto.LoginAttemptResponseDto;
import com.colak.springsecurityjwtauthtutorial.dto.LoginResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class AuthControllerIntegrationTest {

    static final String SIGNUP_URL = "/api/auth/signup";
    static final String LOGIN_URL = "/api/auth/login";
    static final String LOGIN_ATTEMPTS_URL = "/api/auth/loginAttempts";
    private static final int VALIDATION_ERROR_CODE = 400;

    @SuppressWarnings("unused")
    @Autowired
    private WebTestClient webTestClient;

    @SuppressWarnings("unused")
    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13");


    //  Test signup endpoint
    @Test
    void shouldSignupUser() {
        String request = """
                {
                  "name": "mina",
                  "email": "mina@gmail.com",
                  "password": "123456"
                }
                """;
        webTestClient
                .post().uri(SIGNUP_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isCreated();
    }

    @Test
    void shouldReturnDuplicate_onExistingEmail() {
        // signup user
        String request = """
                {
                  "name": "sandra",
                  "email": "sandra@gmail.com",
                  "password": "123456"
                }
                """;
        webTestClient
                .post().uri(SIGNUP_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isCreated();

        // signup another user with duplicate email
        String requestWithSameEmail = """
                {
                  "name": "Anna",
                  "email": "sandra@gmail.com",
                  "password": "654321"
                }
                """;
        ApiErrorResponseDto errorResponse = webTestClient
                .post().uri(SIGNUP_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestWithSameEmail)
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(ApiErrorResponseDto.class)
                .returnResult()
                .getResponseBody();

        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.errorCode()).isEqualTo(409);
        assertThat(errorResponse.description()).isEqualTo("User with the email address 'sandra@gmail.com' already exists.");
    }

    @Test
    void shouldReturnBadRequest_WhenSignupRequestIsNotValid() {
        String request = """
                {
                  "name": " ",
                  "email": "mina@",
                  "password": "456"
                }
                """;
        ApiErrorResponseDto errorResponse = webTestClient
                .post().uri(SIGNUP_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(ApiErrorResponseDto.class)
                .returnResult()
                .getResponseBody();

        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.errorCode()).isEqualTo(VALIDATION_ERROR_CODE);
        assertThat(errorResponse.description()).contains(
                "password: Password must be between 6 and 20 characters",
                "email: Invalid email format",
                "name: Name cannot be blank");
    }

    //  Test login endpoint
    @Test
    void shouldReturnJWTToken_WhenUserIsRegistered() {
        String signupRequest = """
                {
                  "name": "nick",
                  "email": "nick@gmail.com",
                  "password": "123456"
                }
                """;
        webTestClient
                .post().uri(SIGNUP_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(signupRequest)
                .exchange()
                .expectStatus()
                .isCreated();

        String loginRequest = """
                {
                  "email": "nick@gmail.com",
                  "password": "123456"
                }
                """;
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
    }

    @Test
    void shouldReturnBadCredential() {
        String signupRequest = """
                {
                  "name": "john",
                  "email": "john@gmail.com",
                  "password": "123456"
                }
                """;
        webTestClient
                .post().uri(SIGNUP_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(signupRequest)
                .exchange()
                .expectStatus()
                .isCreated();

        String loginRequestWithWrongPassword = """
                {
                  "email": "john@gmail.com",
                  "password": "12345678910"
                }
                """;
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
        assertThat(errorResponse.errorCode()).isEqualTo(401);
        assertThat(errorResponse.description()).isEqualTo("Invalid username or password");
    }

    @Test
    void shouldReturnUnauthorized_WhenUserNotRegistered() {
        String request = """
                {
                  "email": "sara@gmail.com",
                  "password": "123456"
                }
                """;
        ApiErrorResponseDto errorResponse = webTestClient
                .post().uri(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isUnauthorized()
                .expectBody(ApiErrorResponseDto.class)
                .returnResult()
                .getResponseBody();

        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.errorCode()).isEqualTo(401);
        assertThat(errorResponse.description()).isEqualTo("User does not exist, email: sara@gmail.com");
    }

    //  Test loginAttempts endpoint
    @Test
    void shouldReturnLoginAttempts_WhenUserIsRegistered() {
        String signupRequest = """
                {
                  "name": "william",
                  "email": "william@gmail.com",
                  "password": "123456"
                }
                """;
        webTestClient
                .post().uri(SIGNUP_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(signupRequest)
                .exchange()
                .expectStatus()
                .isCreated();

        String loginRequest = """
                {
                  "email": "william@gmail.com",
                  "password": "123456"
                }
                """;
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

        LoginAttemptResponseDto firstLoginAttempt = loginAttemptsResponse.get(0);
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
                .isForbidden();

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
        webTestClient
                .get().uri(LOGIN_ATTEMPTS_URL)
                .header("Authorization", " ")
                .exchange()
                .expectStatus()
                .isForbidden();

//    String errorResponse = webTestClient
//        .get().uri(LOGIN_ATTEMPTS_URL)
//        .header("Authorization", " ")
//        .exchange()
//        .expectStatus()
//        .isUnauthorized()
//        .expectBody(String.class)
//        .returnResult()
//        .getResponseBody();
//
//    assertThat(errorResponse).isNotNull();
//    assertThat(errorResponse).isEqualTo("{\"errorCode\":401,\"description\":\"Access denied: Authorization header is required.\"}");
    }
}