package com.colak.springtutorial.configuration;

import com.colak.springtutorial.dto.ApiErrorResponseDto;
import com.colak.springtutorial.exception.AccessDeniedException;
import com.colak.springtutorial.helper.JwtHelper;
import com.colak.springtutorial.service.userdetails.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    public static final String COOKIE_NAME = "accessToken";
    private final UserDetailsServiceImpl userDetailsService;
    private final ObjectMapper objectMapper;

    public JwtAuthFilter(UserDetailsServiceImpl userDetailsService, ObjectMapper objectMapper) {
        this.userDetailsService = userDetailsService;
        this.objectMapper = objectMapper;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = getTokenFromRequestHeader(httpServletRequest);
            if (token == null) {
                token = getTokenFromCookie(httpServletRequest);
            }

            // If the accessToken is null. It will pass the request to next filter in the chain.
            // Any login and signup requests that does not have jwt token will be passed to next filter chain.
            if (token == null) {
                filterChain.doFilter(httpServletRequest, httpServletResponse);
                return;
            }

            String username = JwtHelper.extractUsername(token);

            validateTokenAndUser(httpServletRequest, username, token);

            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } catch (AccessDeniedException e) {
            ApiErrorResponseDto errorResponse = new ApiErrorResponseDto(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
            httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpServletResponse.getWriter().write(toJson(errorResponse));
        }
    }

    private String getTokenFromRequestHeader(HttpServletRequest httpServletRequest) {
        String token = null;
        String authHeader = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        return token;
    }

    private String getTokenFromCookie(HttpServletRequest httpServletRequest) {
        String token = null;
        if (httpServletRequest.getCookies() != null) {
            for (Cookie cookie : httpServletRequest.getCookies()) {
                if (cookie.getName().equals(COOKIE_NAME)) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        return token;
    }

    private void validateTokenAndUser(HttpServletRequest request, String username, String token) {
        // If any accessToken is present, then it will validate the token and then authenticate the request in security context
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if (username != null && securityContext.getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (JwtHelper.validateToken(token, userDetails)) {

                // Create a token and set to SecurityContext, so that we can be sure that user is authenticated
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails,
                        userDetails.getPassword(), userDetails.getAuthorities());

                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                securityContext.setAuthentication(authenticationToken);
            }
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