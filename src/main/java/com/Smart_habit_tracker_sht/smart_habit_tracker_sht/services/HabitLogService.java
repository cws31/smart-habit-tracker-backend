package com.Smart_habit_tracker_sht.smart_habit_tracker_sht.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.models.HabitLog; // <-- ADD THIS IMPORT
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.DTOs.HabitLogRequest;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.DTOs.HabitLogResponse;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.DTOs.HabitProgressResponse;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.models.Habit;

import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.models.User;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.repositories.HabitLogRepository;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.repositories.HabitRepository;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.repositories.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HabitLogService {

    private final HabitLogRepository habitLogRepository;
    private final HabitRepository habitRepository;
    private final UserRepository userRepository;
    private final HabitProgressService habitProgressService;
    private final emailService emailService;

    // Mark habit as done/skipped
    @Transactional
    public HabitLogResponse markHabit(HabitLogRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Habit habit = habitRepository.findById(request.getHabitId())
                .orElseThrow(() -> new RuntimeException("Habit not found"));

        if (!habit.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("This habit not found for this user " + email);
        }

        LocalDate logDate = request.getLogDate() != null ? request.getLogDate() : LocalDate.now();

        if (logDate.isBefore(habit.getStartDate())) {
            throw new RuntimeException(
                    String.format("Cannot mark habit before start date. Habit starts on: %s",
                            habit.getStartDate()));
        }

        if (habitLogRepository.findByUserAndHabitAndLogDate(user, habit, logDate).isPresent()) {
            throw new RuntimeException("Habit already logged for today!");
        }

        HabitLog habitLog = HabitLog.builder()
                .habit(habit)
                .user(user)
                .logDate(logDate)
                .status(request.getStatus())
                .build();

        HabitLog saved = habitLogRepository.save(habitLog);

        log.info("✅ HABIT LOG CREATED - Habit: {}, User: {}, Status: {}, Date: {}",
                habit.getTitle(), user.getEmail(), request.getStatus(), logDate);

        if (request.getStatus() == HabitLog.Status.DONE) {
            checkAndNotifyChallengeCompletion(habit, user);
        }

        HabitLogResponse response = new HabitLogResponse();
        response.setId(saved.getId());
        response.setHabitId(saved.getHabit().getId());
        response.setHabitTitle(saved.getHabit().getTitle());
        response.setLogDate(saved.getLogDate());
        response.setStatus(saved.getStatus());
        response.setUserId(user.getId());
        return response;
    }

    private void checkAndNotifyChallengeCompletion(Habit habit, User user) {
        try {
            log.debug("📊 CHECKING CHALLENGE COMPLETION - Habit: {}, User: {}",
                    habit.getTitle(), user.getEmail());

            int currentStreak = calculateCurrentStreak(habit);
            int challengeDays = habit.getChallengeDays();

            log.debug("📊 Current Streak: {}/{}, Habit: {}", currentStreak, challengeDays,
                    habit.getTitle());

            if (currentStreak >= challengeDays && challengeDays > 0) {
                log.info("🎉 CHALLENGE COMPLETED! Sending email - Habit: {}, Streak: {}/{}",
                        habit.getTitle(), currentStreak, challengeDays);

                emailService.sendChallengeCompletedEmail(
                        user.getEmail(),
                        user.getName(),
                        habit.getTitle(),
                        currentStreak,
                        challengeDays);

                log.info("✅ Challenge completion email sent to: {}", user.getEmail());
            } else {
                log.debug("⏳ Challenge progress: {}/{} for habit '{}'",
                        currentStreak, challengeDays, habit.getTitle());
            }
        } catch (Exception e) {
            log.error("❌ Failed to check challenge completion for habit {}: {}",
                    habit.getTitle(), e.getMessage());
        }
    }

    // Method to calculate current streak
    private int calculateCurrentStreak(Habit habit) {
        log.debug("🔥 CALCULATING CURRENT STREAK for habit: {}", habit.getTitle());

        List<HabitLog> logs = habitLogRepository.findByHabitOrderByLogDateDesc(habit);
        log.debug("🔥 Total logs found: {}", logs.size());

        if (logs.isEmpty()) {
            log.debug("🔥 No logs found, streak = 0");
            return 0;
        }

        int streak = 0;
        LocalDate currentDate = LocalDate.now();
        log.debug("🔥 Today's date: {}", currentDate);

        for (HabitLog logEntry : logs) {
            LocalDate expectedDate = currentDate.minusDays(streak);

            if (logEntry.getStatus() == HabitLog.Status.DONE &&
                    logEntry.getLogDate().equals(expectedDate)) {
                streak++;
                log.debug("✅ Streak increased to: {}", streak);
            } else {
                log.debug("❌ Streak broken. Expected: {}, Found: {}, Status: {}",
                        expectedDate, logEntry.getLogDate(), logEntry.getStatus());
                break;
            }
        }

        log.debug("🎯 Final calculated streak: {}", streak);
        return streak;
    }

    // Get user habit logs
    public List<HabitLogResponse> getUserHabitLogs() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        List<HabitLogResponse> logs = habitLogRepository.findByUser(user).stream().map(logEntry -> {
            HabitLogResponse res = new HabitLogResponse();
            res.setId(logEntry.getId());
            res.setHabitId(logEntry.getHabit().getId());
            res.setHabitTitle(logEntry.getHabit().getTitle());
            res.setLogDate(logEntry.getLogDate());
            res.setStatus(logEntry.getStatus());
            res.setUserId(logEntry.getUser().getId());
            return res;
        }).collect(Collectors.toList());

        log.debug("📋 Retrieved {} habit logs for user: {}", logs.size(), user.getEmail());
        return logs;
    }

    // In HabitLogService.java - update the auto-skip method
    @Scheduled(cron = "0 59 23 * * *")
    public void markUncompletedHabitsAsSkipped() {
        LocalDate today = LocalDate.now();
        log.info("⏰ Running auto-skip for uncompleted habits on: {}", today);

        int skippedCount = 0;
        for (User user : userRepository.findAll()) {
            for (Habit habit : habitRepository.findByUser(user)) {
                // NEW: Skip if today is before habit start date
                if (today.isBefore(habit.getStartDate())) {
                    continue;
                }

                if (habitLogRepository.findByUserAndHabitAndLogDate(user, habit, today)
                        .orElse(null) == null) {
                    HabitLog skippedLog = HabitLog.builder()
                            .habit(habit)
                            .user(user)
                            .logDate(today)
                            .status(HabitLog.Status.SKIPPED)
                            .build();
                    habitLogRepository.save(skippedLog);
                    skippedCount++;
                }
            }
        }
        log.info("✅ Auto-skip completed. Marked {} habits as skipped", skippedCount);
    }

    // Get habits by longest streak
    @Transactional(readOnly = true)
    public List<HabitProgressResponse> getHabitsByLongestStreak(int targetStreak) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        List<HabitProgressResponse> result = habitProgressService.getHabitsByLongestStreak(user, targetStreak);
        log.debug("📊 Retrieved {} habits with streak >= {} for user: {}",
                result.size(), targetStreak, user.getEmail());

        return result;
    }

    // Get habits with 2-day streak
    public List<HabitProgressResponse> getHabitsWithLongestStreakOfTwo() {
        return getHabitsByLongestStreak(2);
    }

    public String getOverallPerformance() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        String performance = habitProgressService.calculateOverallPerformance(user);
        log.debug("📈 Overall performance for user {}: {}", user.getEmail(), performance);

        return performance;
    }
}