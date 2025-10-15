package com.smart_habit_tracker20.DTOs;

import com.smart_habit_tracker20.models.HabitLog.Status;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
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
