package com.smart_habit_tracker20.DTOs;

import com.smart_habit_tracker20.models.HabitLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HabitProgressResponse {
    private Long habitId;
    private String habitTitle;
    private int totalDays;
    private int completedDays;
    private int skippedDays;
    private double completionRate;
    private Integer streak;
    private Integer longestStreak;
    private LocalDate streakBreakDate;
    private List<DailyLogDTO> dailyHistory;

    // Inner DTO for clean chart data transmission
    @Data
    @AllArgsConstructor
    public static class DailyLogDTO {
        private LocalDate date;
        private HabitLog.Status status;
        private boolean isFuture;
    }
}