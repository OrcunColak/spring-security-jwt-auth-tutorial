package com.colak.springsecurityjwtauthtutorial.configuration;

import com.colak.springsecurityjwtauthtutorial.dto.ApiErrorResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        log.error("Unauthorized Error : {}", authException.getMessage());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiErrorResponseDto apiErrorResponseDto = new ApiErrorResponseDto(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Access denied: Authorization header is required.");
        OutputStream out = response.getOutputStream();
        objectMapper.writeValue(out, apiErrorResponseDto);
        out.flush();

        // This sends "text/html" response
        // response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
    }
}
