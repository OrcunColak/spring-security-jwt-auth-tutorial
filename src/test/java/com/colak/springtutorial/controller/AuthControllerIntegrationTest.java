package com.colak.springtutorial.controller;

import com.colak.springtutorial.dto.ApiErrorResponseDto;
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

import static org.assertj.core.api.Assertions.assertThat;

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


    //  Test signup endpoint
    @Test
    void shouldSignupUser() {
        SignupRequestDto signupRequest = new SignupRequestDto(
                "test@example.com",
                "password123",
                new ArrayList<>()
        );

        webTestClient
                .post().uri(SIGNUP_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(signupRequest)
                .exchange()
                .expectStatus()
                .isCreated();
    }

    @Test
    void shouldReturnDuplicate_onExistingEmail() {
        // signup user
        SignupRequestDto signupRequest = new SignupRequestDto(
                "sandra@gmail.com",
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

        // signup another user with duplicate email
        ApiErrorResponseDto errorResponse = webTestClient
                .post().uri(SIGNUP_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(signupRequest)
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
        // Send bad signup request
        SignupRequestDto signupRequest = new SignupRequestDto(
                "sandra@",
                "123",
                new ArrayList<>()
        );
        ApiErrorResponseDto errorResponse = webTestClient
                .post().uri(SIGNUP_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(signupRequest)
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
                "email: Invalid email format");
    }

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
    }

    @Test
    void shouldReturnBadCredential() {
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