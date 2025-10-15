package com.smart_habit_tracker20.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import com.smart_habit_tracker20.DTOs.UserRegistrationRequest;
import com.smart_habit_tracker20.DTOs.UserResponse;
import com.smart_habit_tracker20.security.JwtUtil;
import com.smart_habit_tracker20.services.UserService;
import com.smart_habit_tracker20.services.emailService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final emailService emailService;

    @GetMapping("/me")
    public ResponseEntity<?> getUserDetails(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.getEmailFromToken(token);
            String name = jwtUtil.getNameFromToken(token);

            Map<String, String> response = new HashMap<>();
            response.put("email", email);
            response.put("name", name);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid or expired token");
        }
    }

    @PostMapping("/auth/register")
    public ResponseEntity<UserResponse> registerUser(
            @Valid @RequestBody UserRegistrationRequest request) {
        UserResponse response = userService.registerUser(request);

        if (response.getStatus().equals("SUCCESS")) {
            try {
                emailService.sendWelcomeEmail(request.getEmail(), request.getName());
            } catch (Exception e) {
                System.err.println("Failed to send welcome email: " + e.getMessage());
            }
        }

        return ResponseEntity.ok(response);
    }
}