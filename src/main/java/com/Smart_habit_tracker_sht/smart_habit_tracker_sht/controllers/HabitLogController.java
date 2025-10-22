package com.Smart_habit_tracker_sht.smart_habit_tracker_sht.controllers;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.DTOs.HabitLogRequest;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.DTOs.HabitLogResponse;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.repositories.HabitLogRepository;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.security.JwtUtil;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.services.HabitLogService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/habitlogs")
@RequiredArgsConstructor
public class HabitLogController {

    private final HabitLogService habitLogService;
    private final JwtUtil jwtUtil;
    private final HabitLogRepository habitLogRepository;

    @PostMapping
    public ResponseEntity<?> markHabit(@RequestBody HabitLogRequest request) {
        try {
            HabitLogResponse response = habitLogService.markHabit(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllLogs() {
        try {
            List<HabitLogResponse> logs = habitLogService.getUserHabitLogs();
            return ResponseEntity.ok(logs);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/done/count")
    public ResponseEntity<Integer> countDoneHabitsToday(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(0);
            }

            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(0);
            }

            String email = jwtUtil.getEmailFromToken(token);
            LocalDate today = LocalDate.now();

            int doneCount = habitLogRepository.countDoneHabitsForToday(email, today);
            return ResponseEntity.ok(doneCount);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(0);
        }
    }

}
