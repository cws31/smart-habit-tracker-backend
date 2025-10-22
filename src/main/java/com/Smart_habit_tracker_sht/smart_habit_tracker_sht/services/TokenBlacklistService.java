package com.Smart_habit_tracker_sht.smart_habit_tracker_sht.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.models.BlacklistedToken;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.repositories.BlacklistedTokenRepository;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.security.JwtUtil;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final JwtUtil jwtUtil;

    // Blacklist a token
    @Transactional
    public void blacklistToken(String token, String reason) {
        try {

            String cleanToken = token.replace("Bearer ", "").trim();

            log.info("🔄 Attempting to blacklist token: {}...",
                    cleanToken.substring(0, Math.min(10, cleanToken.length())));

            if (blacklistedTokenRepository.existsByToken(cleanToken)) {
                log.warn("⚠️ Token already blacklisted: {}...",
                        cleanToken.substring(0, Math.min(20, cleanToken.length())));
                return;
            }

            LocalDateTime expiresAt = jwtUtil.extractExpirationDateTimeFromToken(cleanToken);

            BlacklistedToken blacklistedToken = BlacklistedToken.builder()
                    .token(cleanToken)
                    .blacklistedAt(LocalDateTime.now())
                    .expiresAt(expiresAt)
                    .reason(reason)
                    .build();

            blacklistedTokenRepository.save(blacklistedToken);
            log.info("✅ Token blacklisted successfully. Reason: {}", reason);

        } catch (Exception e) {
            log.error("❌ Failed to blacklist token: {}", e.getMessage());
            throw new RuntimeException("Failed to blacklist token: " + e.getMessage());
        }
    }

    public boolean isTokenBlacklisted(String token) {
        try {

            String cleanToken = token.replace("Bearer ", "").trim();
            boolean isBlacklisted = blacklistedTokenRepository.existsByToken(cleanToken);

            if (isBlacklisted) {
                log.warn("🚫 Token is blacklisted: {}...", cleanToken.substring(0, Math.min(20, cleanToken.length())));
            }

            return isBlacklisted;
        } catch (Exception e) {
            log.error("❌ Error checking token blacklist status: {}", e.getMessage());
            return false;
        }
    }

    // Scheduled cleanup of expired blacklisted tokens (runs daily at 2 AM)
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            LocalDateTime now = LocalDateTime.now();
            long countBefore = blacklistedTokenRepository.count();
            blacklistedTokenRepository.deleteExpiredTokens(now);
            long countAfter = blacklistedTokenRepository.count();
            long deletedCount = countBefore - countAfter;

            log.info("🧹 Cleaned up {} expired blacklisted tokens", deletedCount);
        } catch (Exception e) {
            log.error("❌ Failed to cleanup expired tokens: {}", e.getMessage());
        }
    }

    // Temporary method for debugging - clear all blacklisted tokens
    @Transactional
    public void clearAllBlacklistedTokens() {
        try {
            long count = blacklistedTokenRepository.count();
            blacklistedTokenRepository.deleteAll();
            log.info("🧹 DEBUG: Cleared all {} blacklisted tokens", count);
        } catch (Exception e) {
            log.error("❌ Failed to clear blacklisted tokens: {}", e.getMessage());
        }
    }

    public long getBlacklistedTokensCount() {
        return blacklistedTokenRepository.count();
    }
}