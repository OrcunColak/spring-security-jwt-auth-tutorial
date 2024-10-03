package com.colak.springtutorial.configuration;

import com.colak.springtutorial.dto.ApiErrorResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final AuthenticationManager authenticationManager;
    private final RequestMatcher requestMatcher = AnyRequestMatcher.INSTANCE;

    private final BearerAuthenticationConverter authenticationConverter = new BearerAuthenticationConverter();

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain)
            throws IOException {
        try {
            // Do not attempt to authenticate if the filter does not match the route
            if (!requestMatcher.matches(httpServletRequest)) {
                filterChain.doFilter(httpServletRequest, httpServletResponse);
                return;
            }

            // Do not attempt to authenticate if authentication is not present
            Authentication authentication = authenticationConverter.convert(httpServletRequest);
            if (authentication == null) {
                filterChain.doFilter(httpServletRequest, httpServletResponse);
                return;
            }

            // Do not attempt to authenticate if request is already authenticated
            // Authentication existingAuthentication = SecurityContextHolder.getContext().getAuthentication();
            // if (existingAuthentication != null && existingAuthentication.isAuthenticated()) {
            //     filterChain.doFilter(httpServletRequest, httpServletResponse);
            //     return;
            // }

            // Perform the authentication and set it in the security context
            Authentication populatedAuthentication = authenticationManager.authenticate(authentication);
            SecurityContextHolder.getContext().setAuthentication(populatedAuthentication);
            httpServletRequest.setAttribute("isAuthenticated", true);

            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } catch (Exception exception) {
            ApiErrorResponseDto errorResponse = new ApiErrorResponseDto(HttpServletResponse.SC_FORBIDDEN, exception.getMessage());
            httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpServletResponse.getWriter().write(toJson(errorResponse));
        }
    }

    private String toJson(ApiErrorResponseDto response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            return ""; // Return an empty string if serialization fails
        }
    }
}
