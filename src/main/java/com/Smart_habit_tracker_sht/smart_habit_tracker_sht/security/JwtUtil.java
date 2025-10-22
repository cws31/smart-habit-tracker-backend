package com.Smart_habit_tracker_sht.smart_habit_tracker_sht.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.function.Function;

@Slf4j
@Component
public class JwtUtil {

    private Key key;
    private final long jwtExpirationMs = 7 * 24 * 60 * 60 * 1000;

    @PostConstruct
    public void init() {
        String secret = "smart-habit-tracker-secret-key-2024-very-secure-and-long";
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String email, String name) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(email)
                .claim("name", name)
                .claim("email", email)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            String jwtToken = cleanToken(token);

            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(jwtToken);

            log.debug("Token validation successful");
            return true;

        } catch (ExpiredJwtException ex) {
            log.error("JWT token expired: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        } catch (SecurityException ex) {
            log.error("JWT signature validation failed: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        } catch (JwtException ex) {
            log.error("JWT validation failed: {}", ex.getMessage());
        }
        return false;
    }

    // Separate method for signature validation only (without blacklist check)
    public boolean validateTokenSignature(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(cleanToken(token));
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        return extractClaim(cleanToken(token), Claims::getSubject);
    }

    public String getNameFromToken(String token) {
        return extractClaim(cleanToken(token), claims -> claims.get("name", String.class));
    }

    public Date getExpirationFromToken(String token) {
        return extractClaim(cleanToken(token), Claims::getExpiration);
    }

    public LocalDateTime extractExpirationDateTimeFromToken(String token) {
        try {
            Date expirationDate = getExpirationFromToken(token);
            return LocalDateTime.ofInstant(expirationDate.toInstant(), ZoneId.systemDefault());
        } catch (Exception e) {
            log.error("Could not extract expiration from token: {}", e.getMessage());
            return LocalDateTime.now().plusHours(24);
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            log.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(cleanToken(token))
                    .getBody();
        } catch (ExpiredJwtException ex) {
            log.warn("⚠️ Token expired but returning claims for logging: {}", ex.getMessage());
            return ex.getClaims();
        }
    }

    private String cleanToken(String token) {
        if (token == null) {
            throw new IllegalArgumentException("Token cannot be null");
        }
        if (token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }
}