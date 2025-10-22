package com.Smart_habit_tracker_sht.smart_habit_tracker_sht.DTOs;

import lombok.*;

import java.time.LocalDate;

import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.models.HabitLog.Status;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HabitLogResponse {
    private Long id;
    private Long habitId;
    private String habitTitle;
    private LocalDate logDate;
    private Status status;
    private Long userId;
}
