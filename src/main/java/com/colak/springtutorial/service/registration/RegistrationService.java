package com.colak.springtutorial.service.registration;

import com.colak.springtutorial.dto.signup.SignupRequestDto;
import com.colak.springtutorial.entity.User;
import com.colak.springtutorial.exception.DuplicateException;
import com.colak.springtutorial.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;


    @Transactional
    public void signup(SignupRequestDto signupRequest) {
        String email = signupRequest.email();
        Optional<User> existingUser = repository.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new DuplicateException(String.format("User with the email address '%s' already exists.", email));
        }

        String hashedPassword = passwordEncoder.encode(signupRequest.password());
        User user = new User(signupRequest.name(), email, hashedPassword);
        repository.add(user);
    }
}
