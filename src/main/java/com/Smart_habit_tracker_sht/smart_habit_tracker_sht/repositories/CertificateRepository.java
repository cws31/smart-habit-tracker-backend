package com.Smart_habit_tracker_sht.smart_habit_tracker_sht.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.models.Certificate;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.models.User;

import java.util.List;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    List<Certificate> findByUserAndDeliveredFalse(User user);

    List<Certificate> findByUser(User user);

    boolean existsByHabitIdAndStreakAchieved(Long habitId, int streakAchieved);
}
