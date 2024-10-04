package com.colak.springtutorial.service.refreshtokenservice;

import com.colak.springtutorial.jpa.RefreshToken;
import com.colak.springtutorial.jpa.User;
import com.colak.springtutorial.repository.RefreshTokenRepository;
import com.colak.springtutorial.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final UserRepository userRepository;

    @Transactional
    public String createRefreshToken(String email){
        Optional<User> optionalUser = userRepository.findByEmail(email);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(optionalUser.get())
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plus(Duration.ofHours(24)))
                .build();
        RefreshToken savedRefreshToken = refreshTokenRepository.save(refreshToken);
        return savedRefreshToken.getToken();
    }

    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String token){
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token){
        if(token.getExpiryDate().compareTo(Instant.now())<0){
            refreshTokenRepository.delete(token);
            throw new RuntimeException(token.getToken() + " Refresh token is expired. Please make a new login..!");
        }
        return token;
    }
}
