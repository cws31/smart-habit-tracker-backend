package com.Smart_habit_tracker_sht.smart_habit_tracker_sht.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateDTO {
    private Long id;
    private Long userId;
    private Long habitId;
    private String habitTitle;
    private String userName;
    private String certificateMessage;
    private int streakAchieved;
    private LocalDateTime awardedAt;
    private LocalDate streakCompletionDate;
    private boolean delivered;
    private String type;
}