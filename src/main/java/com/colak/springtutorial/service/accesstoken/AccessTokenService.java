package com.colak.springtutorial.service.accesstoken;

import com.colak.springtutorial.helper.JwtHelper;
import org.springframework.stereotype.Service;

@Service
public class AccessTokenService {

    public String generateAccessToken (String email) {
        return JwtHelper.generateAccessToken(email);
    }
}
