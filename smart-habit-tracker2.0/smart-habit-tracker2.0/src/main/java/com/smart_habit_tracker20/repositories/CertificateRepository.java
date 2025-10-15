package com.smart_habit_tracker20.repositories;

import com.smart_habit_tracker20.models.Certificate;
import com.smart_habit_tracker20.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    List<Certificate> findByUserAndDeliveredFalse(User user);

    List<Certificate> findByUser(User user);

    boolean existsByHabitIdAndStreakAchieved(Long habitId, int streakAchieved);
}