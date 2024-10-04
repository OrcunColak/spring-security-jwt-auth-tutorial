package com.colak.springtutorial.controller;

import com.colak.springtutorial.dto.refreshtoken.RefreshTokenRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class RefreshControllerIT {

    static final String REFRESH_URL = "/api/auth/refreshToken";

    @SuppressWarnings("unused")
    @Autowired
    private WebTestClient webTestClient;


    @Test
    void shouldReturnNotFound_WhenRefreshTokenDoesNotExist() {
        RefreshTokenRequestDTO refreshTokenRequestDTO = new RefreshTokenRequestDTO("fake-refresh-token");

        ProblemDetail errorResponse = webTestClient
                .post().uri(REFRESH_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(refreshTokenRequestDTO)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(ProblemDetail.class)
                .returnResult()
                .getResponseBody();

        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(errorResponse.getDetail()).isEqualTo("Refresh Token is not in DB..!!");
    }
}