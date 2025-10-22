package com.Smart_habit_tracker_sht.smart_habit_tracker_sht.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Habit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String frequency;
    private LocalDate startDate;

    @Column(name = "challenge_days")
    private Integer challengeDays = 21;

    private int challengeLevel = 0;
    private LocalDate lastCertificateDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Integer getChallengeDays() {
        return challengeDays != null ? challengeDays : 21;
    }
}
