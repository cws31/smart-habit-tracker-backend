package com.Smart_habit_tracker_sht.smart_habit_tracker_sht.DTOs;

import lombok.*;

import java.time.LocalDate;

import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.models.HabitLog.Status;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HabitLogRequest {
    private Long habitId;
    private LocalDate logDate;
    private Status status;
}
