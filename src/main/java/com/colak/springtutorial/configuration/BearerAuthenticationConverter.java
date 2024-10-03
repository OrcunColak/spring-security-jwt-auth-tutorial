package com.colak.springtutorial.configuration;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;

public class BearerAuthenticationConverter implements AuthenticationConverter {

    public static final String COOKIE_NAME = "accessToken";
    public static final String BEARER_PREFIX = "Bearer ";

    @Override
    public Authentication convert(HttpServletRequest request) {
        String token = getTokenFromRequestHeader(request);
        if (token == null) {
            token = getTokenFromCookie(request);
        }
        if (token != null) {
            return new JwtAuthenticationToken(token);
        }
        return null;
    }

    private String getTokenFromRequestHeader(HttpServletRequest httpServletRequest) {
        String token = null;
        String authHeader = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
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
}
