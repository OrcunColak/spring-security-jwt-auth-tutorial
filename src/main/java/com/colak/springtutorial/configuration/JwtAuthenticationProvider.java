package com.colak.springtutorial.configuration;

import com.colak.springtutorial.helper.JwtHelper;
import com.colak.springtutorial.service.userdetails.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsServiceImpl userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String token = (String) authentication.getCredentials();
        if (JwtHelper.isTokenExpired(token)) {
            return null;
        }
        String username = JwtHelper.extractUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (userDetails == null) {
            return null;
        }

        return new JwtAuthenticationToken(userDetails, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(JwtAuthenticationToken.class);
    }
}
