package com.colak.springsecurityjwtauthtutorial.service;

import com.colak.springsecurityjwtauthtutorial.dto.SignupRequestDto;
import com.colak.springsecurityjwtauthtutorial.entity.User;
import com.colak.springsecurityjwtauthtutorial.exception.DuplicateException;
import com.colak.springsecurityjwtauthtutorial.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;


    @Transactional
    public void signup(SignupRequestDto request) {
        String email = request.email();
        Optional<User> existingUser = repository.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new DuplicateException(String.format("User with the email address '%s' already exists.", email));
        }

        String hashedPassword = passwordEncoder.encode(request.password());
        User user = new User(request.name(), email, hashedPassword);
        repository.add(user);
    }

}
