package com.colak.springtutorial.service.registration;

import com.colak.springtutorial.dto.signup.SignupRequestDto;
import com.colak.springtutorial.exception.DuplicateException;
import com.colak.springtutorial.jpa.Role;
import com.colak.springtutorial.jpa.User;
import com.colak.springtutorial.repository.RoleRepository;
import com.colak.springtutorial.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;


    @Transactional
    public User signup(SignupRequestDto signupRequest) {
        String email = signupRequest.email();
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new DuplicateException(String.format("User with the email address '%s' already exists.", email));
        }

        String hashedPassword = passwordEncoder.encode(signupRequest.password());
        User user = new User();
        user.setEmail(email);
        user.setPassword(hashedPassword);

        // Split the roles by comma, trim each role, and fetch or create from the database
        Set<Role> roles = signupRequest.roles().stream()
                .map(String::trim) // Trim whitespace
                .map(roleName -> roleRepository.findByAuthority(roleName)
                        .orElseGet(() -> createNewRole(roleName))) // Create new role if not found
                .collect(Collectors.toSet());

        // Set the roles for the user
        user.setAuthorities(roles);

        return userRepository.save(user);
    }

    private Role createNewRole(String roleName) {
        Role newRole = new Role();
        newRole.setAuthority(roleName);
        return roleRepository.save(newRole);
    }
}
