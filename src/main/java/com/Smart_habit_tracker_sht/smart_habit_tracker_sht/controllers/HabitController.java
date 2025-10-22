package com.Smart_habit_tracker_sht.smart_habit_tracker_sht.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.DTOs.HabitRequest;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.DTOs.HabitResponse;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.DTOs.SetChallengeRequest;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.repositories.HabitRepository;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.security.JwtUtil;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.services.HabitService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/habits")
@RequiredArgsConstructor
public class HabitController {

    private final HabitService habitService;
    private final HabitRepository habitRepository;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<HabitResponse> createHabit(@Valid @RequestBody HabitRequest request) {
        HabitResponse response = habitService.createHabit(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<HabitResponse>> getUserHabits() {
        List<HabitResponse> habits = habitService.getHabitsForLoggedInUser();
        return ResponseEntity.ok(habits);
    }

    @PutMapping("/{habitId}")
    public ResponseEntity<HabitResponse> updateHabit(@PathVariable Long habitId,
            @Valid @RequestBody HabitRequest request) {
        HabitResponse response = habitService.updateHabit(habitId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/set-challenge")
    public ResponseEntity<HabitResponse> setCustomChallenge(@Valid @RequestBody SetChallengeRequest request) {
        HabitResponse response = habitService.setCustomChallenge(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update-challenge")
    public ResponseEntity<HabitResponse> updateChallenge(@Valid @RequestBody SetChallengeRequest request) {
        HabitResponse response = habitService.updateChallenge(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{habitId}/challenge-history")
    public ResponseEntity<List<Map<String, Object>>> getChallengeHistory(@PathVariable Long habitId) {
        List<Map<String, Object>> history = habitService.getChallengeHistory(habitId);
        return ResponseEntity.ok(history);
    }

    @DeleteMapping("/{habitId}")
    public ResponseEntity<Void> deleteHabit(@PathVariable Long habitId) {
        habitService.deleteHabit(habitId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> countHabits(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(0);
            }

            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(0);
            }

            String email = jwtUtil.getEmailFromToken(token);

            int habitCount = habitRepository.countByUserEmail(email);

            return ResponseEntity.ok(habitCount);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(0);
        }
    }
}