package com.colak.springtutorial.controller;

import com.colak.springtutorial.dto.login.LoginResponseDto;
import com.colak.springtutorial.dto.refreshtoken.RefreshTokenRequestDTO;
import com.colak.springtutorial.exception.NotFoundException;
import com.colak.springtutorial.jpa.RefreshToken;
import com.colak.springtutorial.service.accesstoken.AccessTokenService;
import com.colak.springtutorial.service.refreshtokenservice.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/auth")
@RequiredArgsConstructor
public class RefreshTokenController {

    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/refreshToken")
    public LoginResponseDto refreshToken(@RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO) {
        return refreshTokenService.findByToken(refreshTokenRequestDTO.token())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(userInfo -> {
                    String email = userInfo.getEmail();
                    String accessToken = accessTokenService.generateAccessToken(email);
                    String refreshToken = refreshTokenService.createRefreshToken(email);
                    return new LoginResponseDto(userInfo.getEmail(), accessToken, refreshToken);

                })
                .orElseThrow(() -> new NotFoundException("Refresh Token is not in DB..!!"));
    }
}
