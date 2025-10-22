package com.Smart_habit_tracker_sht.smart_habit_tracker_sht.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.models.ChallengeHistory;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.models.Habit;

import java.time.LocalDate;
import java.util.*;

public interface ChallengeHistoryRepository extends JpaRepository<ChallengeHistory, Long> {
    List<ChallengeHistory> findByHabitOrderByChangedAtDesc(Habit habit);

    @Query("SELECT ch FROM ChallengeHistory ch WHERE ch.habit.id = :habitId AND ch.type = 'COMPLETED' AND ch.previousDays = :challengeDays AND DATE(ch.changedAt) = :today")
    List<ChallengeHistory> findRecentCompletions(@Param("habitId") Long habitId,
            @Param("challengeDays") int challengeDays,
            @Param("today") LocalDate today);

    void deleteByHabitId(Long habitId);

    @Query("SELECT ch FROM ChallengeHistory ch WHERE ch.habit.id = :habitId ORDER BY ch.changedAt DESC")
    List<ChallengeHistory> findByHabitIdOrderByChangedAtDesc(@Param("habitId") Long habitId);


    // Find all completed challenges for a habit
    List<ChallengeHistory> findByHabitAndTypeOrderByChangedAtDesc(Habit habit, String type);


    // Find all completed challenges for a user across all habits
    @Query("SELECT ch FROM ChallengeHistory ch WHERE ch.habit.user.id = :userId AND ch.type = 'COMPLETED' ORDER BY ch.changedAt DESC")
    List<ChallengeHistory> findCompletedChallengesByUserId(@Param("userId") Long userId);
}