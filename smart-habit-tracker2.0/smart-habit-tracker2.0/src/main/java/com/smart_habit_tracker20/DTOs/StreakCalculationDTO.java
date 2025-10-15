package com.smart_habit_tracker20.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreakCalculationDTO {
    private Long habitId;
    private String habitTitle;
    private int longestStreak;
    private int currentStreak;
    private LocalDate streakEndDate;
}