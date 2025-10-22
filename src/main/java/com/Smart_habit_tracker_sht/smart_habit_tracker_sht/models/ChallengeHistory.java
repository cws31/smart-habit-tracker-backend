package com.Smart_habit_tracker_sht.smart_habit_tracker_sht.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "challenge_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "habit_id")
    private Habit habit;

    private int previousDays;
    private int newDays;
    private int currentStreak;
    private LocalDateTime changedAt;
    private String type;
}