package com.Smart_habit_tracker_sht.smart_habit_tracker_sht.models;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "certificates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Certificate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habit_id")
    private Habit habit;

    @Column(nullable = false, length = 2000)
    private String certificateMessage;

    @Column(nullable = false)
    private int streakAchieved;

    @Column(nullable = false)
    private LocalDateTime awardedAt;

    @Column(nullable = false)
    private LocalDate streakCompletionDate;

    @Column(nullable = false)
    private boolean delivered;

    @Enumerated(EnumType.STRING)
    private CertificateType type;

    public enum CertificateType {
        STREAK_MILESTONE, CHALLENGE_COMPLETION, CONSISTENCY_AWARD
    }
}