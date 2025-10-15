package com.smart_habit_tracker20.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.*;
import com.smart_habit_tracker20.models.ChallengeHistory;
import com.smart_habit_tracker20.models.Habit;

public interface ChallengeHistoryRepository extends JpaRepository<ChallengeHistory, Long> {
    List<ChallengeHistory> findByHabitOrderByChangedAtDesc(Habit habit);

    @Query("SELECT ch FROM ChallengeHistory ch WHERE ch.habit.id = :habitId AND ch.type = 'COMPLETED' AND ch.previousDays = :challengeDays AND DATE(ch.changedAt) = :today")
    List<ChallengeHistory> findRecentCompletions(@Param("habitId") Long habitId,
            @Param("challengeDays") int challengeDays,
            @Param("today") LocalDate today);

    void deleteByHabitId(Long habitId);

    @Query("SELECT ch FROM ChallengeHistory ch WHERE ch.habit.id = :habitId ORDER BY ch.changedAt DESC")
    List<ChallengeHistory> findByHabitIdOrderByChangedAtDesc(@Param("habitId") Long habitId);
}