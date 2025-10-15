package com.smart_habit_tracker20.controllers;

import com.smart_habit_tracker20.DTOs.HabitProgressResponse;
import com.smart_habit_tracker20.services.HabitLogService;
import com.smart_habit_tracker20.services.HabitProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
