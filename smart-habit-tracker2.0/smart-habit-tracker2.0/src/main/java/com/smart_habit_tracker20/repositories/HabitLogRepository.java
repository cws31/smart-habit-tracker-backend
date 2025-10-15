package com.smart_habit_tracker20.repositories;

import com.smart_habit_tracker20.models.HabitLog;
import com.smart_habit_tracker20.models.User;
import com.smart_habit_tracker20.models.Habit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface HabitLogRepository extends JpaRepository<HabitLog, Long> {

    Optional<HabitLog> findByUserAndHabitAndLogDate(User user, Habit habit, LocalDate logDate);

    List<HabitLog> findByUser(User user);

    List<HabitLog> findByHabit(Habit habit);

    List<HabitLog> findByHabitAndLogDate(Habit habit, LocalDate logDate);

    List<HabitLog> findByHabitIdAndLogDate(Long habitId, LocalDate logDate);

    List<HabitLog> findByHabitAndUserAndLogDateBetweenOrderByLogDateAsc(Habit habit, User user, LocalDate start,
            LocalDate end);

    List<HabitLog> findByHabitAndUserOrderByLogDateAsc(Habit habit, User user);

    List<HabitLog> findByHabitOrderByLogDateAsc(Habit habit);

    @Query("SELECT COUNT(hl) FROM HabitLog hl WHERE hl.user.email = :email AND hl.status = 'DONE' AND hl.logDate = :today")
    int countDoneHabitsForToday(@Param("email") String email, @Param("today") LocalDate today);

    List<HabitLog> findByHabitOrderByLogDateDesc(Habit habit);

    List<HabitLog> findByHabitAndLogDateGreaterThanEqual(Habit habit, LocalDate startDate);

}
