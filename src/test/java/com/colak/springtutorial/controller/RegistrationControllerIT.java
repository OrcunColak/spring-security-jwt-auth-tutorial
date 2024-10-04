package com.colak.springtutorial.controller;

import com.colak.springtutorial.dto.signup.SignupRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class RegistrationControllerIT {

    static final String SIGNUP_URL = "/api/auth/signup";

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
        ProblemDetail errorResponse = webTestClient
                .post().uri(SIGNUP_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(signupRequest)
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(ProblemDetail.class)
                .returnResult()
                .getResponseBody();

        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus()).isEqualTo(409);
        assertThat(errorResponse.getDetail()).isEqualTo("User with the email address 'sandra@gmail.com' already exists.");
    }

    @Test
    void shouldReturnBadRequest_WhenSignupRequestIsNotValid() {
        // Send bad signup request
        SignupRequestDto signupRequest = new SignupRequestDto(
                "sandra@",
                "123",
                new ArrayList<>()
        );
        ProblemDetail errorResponse = webTestClient
                .post().uri(SIGNUP_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(signupRequest)
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(ProblemDetail.class)
                .returnResult()
                .getResponseBody();

        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(errorResponse.getDetail()).contains(
                "password: Password must be between 6 and 20 characters",
                "email: Invalid email format");
    }
}