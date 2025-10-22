package com.Smart_habit_tracker_sht.smart_habit_tracker_sht.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.DTOs.HabitProgressResponse;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.services.HabitLogService;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.services.HabitProgressService;

@RestController
@RequestMapping("/api/habits")
@RequiredArgsConstructor
public class HabitProgressController {

    private final HabitProgressService habitProgressService;
    private final HabitLogService habitLogService;

    @GetMapping("/{habitId}/progress")
    public ResponseEntity<HabitProgressResponse> getHabitProgress(@PathVariable Long habitId) {
        return ResponseEntity.ok(habitProgressService.getHabitProgress(habitId));
    }

    @GetMapping("/overall-performance")
    public String getOverallPerformance() {
        return habitLogService.getOverallPerformance();
    }

}
