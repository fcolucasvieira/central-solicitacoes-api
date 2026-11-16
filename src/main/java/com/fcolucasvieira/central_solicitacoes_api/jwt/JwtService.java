package com.fcolucasvieira.central_solicitacoes_api.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    @Value("${app.security.jwt.secret-key}")
    private String secret;

    @Value("${app.security.jwt.expiration-ms}")
    private long expirationMs;

    @Value("${app.security.jwt.issuer}")
    private String issuer;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UserDetails user) {
        Date issuance = new Date();
        Date expiration = new Date(issuance.getTime() + expirationMs);

        return Jwts.builder()
                .issuer(issuer)
                .subject(user.getUsername())
                .issuedAt(issuance)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token, UserDetails user) {
        Claims claims = extractClaims(token);

        Date now = new Date();

        return claims.getExpiration().after(now)
                && claims.getSubject().equals(user.getUsername());
    }
}
