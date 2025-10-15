package com.smart_habit_tracker20.DTOs;

import com.smart_habit_tracker20.models.HabitLog.Status;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HabitLogRequest {
    private Long habitId;
    private LocalDate logDate;
    private Status status;
}
