package com.colak.springsecurityjwtauthtutorial.helper;

import com.colak.springsecurityjwtauthtutorial.exception.AccessDeniedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@UtilityClass
public class JwtHelper {

    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final int MINUTES = 60;

    /**
     *  This method is used to generate a JWT token.
     *  It takes an input, creates a set of claims (e.g., subject, issued-at, expiration), and then builds a JWT
     *  token using the claims and the signing key. The resulting token is returned.
     */
    public static String generateToken(String email) {
        var now = Instant.now();
        return Jwts.builder()
                .subject(email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(MINUTES, ChronoUnit.MINUTES)))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public static boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * This method takes a JWT token as input and extracts the subject (usually the username) from the token’s claims.
     */
    public static String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    private static boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        return expiration.before(new Date());
    }

    /**
     * This method extracts the expiration date from the JWT token’s claims
     */
    public Date extractExpiration(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getExpiration();
    }

    /**
     * his method parses the JWT token and extracts all of its claims.
     * It uses the `Jwts` builder to create a parser that is configured with the appropriate signing key and then extracts the token’s claims.
     */
    private static Claims extractAllClaims(String token) {
        try {
            return Jwts
                    .parser()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (SignatureException | ExpiredJwtException e) { // Invalid signature or expired token
            throw new AccessDeniedException("Access denied: " + e.getMessage());
        }
    }
}
