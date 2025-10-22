package com.Smart_habit_tracker_sht.smart_habit_tracker_sht.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.models.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    void deleteByEmail(String email);
}
