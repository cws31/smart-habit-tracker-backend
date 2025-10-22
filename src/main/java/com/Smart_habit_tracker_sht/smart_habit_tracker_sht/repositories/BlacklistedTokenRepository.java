package com.Smart_habit_tracker_sht.smart_habit_tracker_sht.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.models.BlacklistedToken;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {

    Optional<BlacklistedToken> findByToken(String token);

    boolean existsByToken(String token);

    @Modifying
    @Query("DELETE FROM BlacklistedToken bt WHERE bt.expiresAt < :currentTime")
    void deleteExpiredTokens(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT COUNT(bt) FROM BlacklistedToken bt WHERE bt.reason = 'LOGOUT'")
    long countLogoutTokens();
}
