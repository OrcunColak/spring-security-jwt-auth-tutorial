package com.colak.springsecurityjwtauthtutorial.service;

import com.colak.springsecurityjwtauthtutorial.entity.User;
import com.colak.springsecurityjwtauthtutorial.exception.NotFoundException;
import com.colak.springsecurityjwtauthtutorial.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository repository;


    @Override
    public UserDetails loadUserByUsername(String email) {

        User user = repository.findByEmail(email).orElseThrow(() ->
                new NotFoundException(String.format("User does not exist, email: %s", email)));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.email())
                .password(user.password())
                .build();
    }
}
