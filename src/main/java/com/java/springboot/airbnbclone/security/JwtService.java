package com.java.springboot.airbnbclone.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    @Value("${spring.security.secretKey}")
    private String secretKey;

    public String generateAccessToken(String email) {
        return Jwts.builder().subject(email).issuedAt(new Date()).expiration((new Date(System.currentTimeMillis() + 1000*60*10)))
                .signWith(generateKey()).compact();

    }

    public String generateRefreshToken(String email) {
        return Jwts.builder().subject(email).issuedAt(new Date()).expiration((new Date(System.currentTimeMillis() + 1000L*60*60*24*30*6)))
                .signWith(generateKey()).compact();

    }

    private SecretKey generateKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String validateToken(String token) {

        return Jwts.parser().verifyWith(generateKey()).build().parseSignedClaims(token).getPayload().getSubject();
    }
}
