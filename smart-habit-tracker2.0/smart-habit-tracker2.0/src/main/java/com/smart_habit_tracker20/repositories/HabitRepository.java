package com.smart_habit_tracker20.repositories;

import com.smart_habit_tracker20.models.Habit;
import com.smart_habit_tracker20.models.User;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HabitRepository extends JpaRepository<Habit, Long> {
    List<Habit> findByUserId(Long userId);

    List<Habit> findByUser(User user);

    int countByUserEmail(String email);
}
