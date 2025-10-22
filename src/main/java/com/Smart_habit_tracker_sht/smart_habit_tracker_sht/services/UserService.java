package com.Smart_habit_tracker_sht.smart_habit_tracker_sht.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.DTOs.UserRegistrationRequest;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.DTOs.UserResponse;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.models.User;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final emailService emailService;

    public UserResponse registerUser(UserRegistrationRequest request) {
        log.info(" Registering new user: {}", request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn(" Email already registered: {}", request.getEmail());
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);
        log.info(" User registered successfully: {}", savedUser.getEmail());

        try {
            emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getName());
            log.info(" Welcome email sent to: {}", savedUser.getEmail());
        } catch (Exception e) {
            log.error(" Failed to send welcome email to {}: {}", savedUser.getEmail(), e.getMessage());
        }

        UserResponse response = new UserResponse();
        response.setStatus("SUCCESS");
        response.setMessage("User registered successfully");
        response.setId(savedUser.getId());
        response.setName(savedUser.getName());
        response.setEmail(savedUser.getEmail());

        return response;
    }
}