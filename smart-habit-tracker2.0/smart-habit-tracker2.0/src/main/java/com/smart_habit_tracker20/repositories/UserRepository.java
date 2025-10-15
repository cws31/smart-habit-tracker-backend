package com.smart_habit_tracker20.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.smart_habit_tracker20.models.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    void deleteByEmail(String email);
}
