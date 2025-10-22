package com.Smart_habit_tracker_sht.smart_habit_tracker_sht.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.DTOs.AuthenticationRequest;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.DTOs.AuthenticationResponse;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.repositories.UserRepository;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.security.CustomUserDetails;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.security.JwtUtil;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.services.TokenBlacklistService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

            String jwt = jwtUtil.generateToken(userDetails.getEmail(), userDetails.getFullName());

            log.info(" User logged in successfully: {}", userDetails.getEmail());

            return ResponseEntity.ok(
                    new AuthenticationResponse(jwt, "Bearer", userDetails.getFullName(), userDetails.getEmail()));
        } catch (BadCredentialsException ex) {
            log.warn(" Login failed - invalid credentials for: {}", request.getEmail());
            return ResponseEntity.status(401).body("Invalid email or password");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest request) {

        try {

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "ERROR",
                        "message", "No valid authorization token provided"));
            }

            String token = authHeader.substring(7);

            tokenBlacklistService.blacklistToken(token, "LOGOUT");

            log.info(" User logged out successfully. Token blacklisted.");

            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Logged out successfully");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error(" Logout failed: {}", e.getMessage());

            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "message", "Logout failed: " + e.getMessage()));
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();

            Map<String, Object> response = new HashMap<>();
            response.put("status", "VALID");
            response.put("message", "Token is valid");
            response.put("email", email);
            response.put("timestamp", System.currentTimeMillis());

            log.info(" Token validation successful for: {}", email);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error(" Token validation failed: {}", e.getMessage());

            Map<String, String> error = new HashMap<>();
            error.put("status", "INVALID");
            error.put("message", "Token validation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @GetMapping("/debug/token-status")
    public ResponseEntity<?> debugTokenStatus(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of("error", "No Bearer token provided"));
            }

            String token = authHeader.substring(7);
            Map<String, Object> status = new HashMap<>();
            status.put("token_preview", token.substring(0, Math.min(20, token.length())) + "...");
            status.put("is_blacklisted", tokenBlacklistService.isTokenBlacklisted(token));
            status.put("is_valid", jwtUtil.validateToken(token));
            status.put("blacklisted_tokens_count", tokenBlacklistService.getBlacklistedTokensCount());

            if (jwtUtil.validateToken(token)) {
                status.put("email", jwtUtil.getEmailFromToken(token));
                status.put("expiration", jwtUtil.getExpirationFromToken(token));
            }

            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/debug/clear-blacklist")
    public ResponseEntity<?> clearBlacklist() {
        try {
            tokenBlacklistService.clearAllBlacklistedTokens();
            return ResponseEntity.ok(Map.of(
                    "message", "All blacklisted tokens cleared",
                    "timestamp", System.currentTimeMillis()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}