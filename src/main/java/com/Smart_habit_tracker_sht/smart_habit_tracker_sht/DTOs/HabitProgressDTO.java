package com.Smart_habit_tracker_sht.smart_habit_tracker_sht.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HabitProgressDTO {

    private Long habitId;
    private int streak;
    private int longestStreak;
    private int currentCycleProgress;
}
