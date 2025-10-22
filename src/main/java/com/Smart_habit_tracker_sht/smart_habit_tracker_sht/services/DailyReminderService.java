package com.Smart_habit_tracker_sht.smart_habit_tracker_sht.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.DTOs.HabitReminderDTO;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.models.Habit;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.models.HabitLog;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.models.User;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.repositories.HabitRepository;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.repositories.UserRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyReminderService {

    private final UserRepository userRepository;
    private final HabitRepository habitRepository;
    private final HabitService habitService;
    private final emailService emailService;
    private final HabitProgressService habitProgressService;

    // Send daily reminders at 8:00 AM every day
    @Scheduled(cron = "0 0 8 * * ?")
    public void sendDailyReminders() {
        try {
            log.info("🕗 Starting daily habit reminders...");

            List<User> allUsers = userRepository.findAll();
            int totalEmailsSent = 0;
            int totalUsers = allUsers.size();

            log.info("👥 Processing {} users for daily reminders", totalUsers);

            for (User user : allUsers) {
                try {
                    List<HabitReminderDTO> pendingHabits = getPendingHabitsForToday(user);

                    if (!pendingHabits.isEmpty()) {
                        emailService.sendDailyReminderEmail(
                                user.getEmail(),
                                user.getName(),
                                pendingHabits);
                        totalEmailsSent++;
                        log.info("Sent reminder to {} with {} habits", user.getEmail(),
                                pendingHabits.size());
                    } else {
                        log.info(" No pending habits for {}", user.getEmail());
                    }

                    Thread.sleep(100);

                } catch (Exception e) {
                    log.error(" Failed to process user {}: {}", user.getEmail(),
                            e.getMessage());
                }
            }

            log.info(" Daily reminders completed: {}/{} emails sent", totalEmailsSent,
                    totalUsers);

        } catch (Exception e) {
            log.error(" Daily reminder job failed: {}", e.getMessage());
        }
    }

    // Get all pending habits for a user for today
    public List<HabitReminderDTO> getPendingHabitsForToday(User user) {
        List<HabitReminderDTO> pendingHabits = new ArrayList<>();

        try {
            List<Habit> userHabits = habitRepository.findByUser(user);
            LocalDate today = LocalDate.now();

            for (Habit habit : userHabits) {

                if (isHabitPendingForToday(habit, today)) {
                    int currentStreak = calculateCurrentStreak(habit);

                    HabitReminderDTO reminderDTO = new HabitReminderDTO(
                            habit.getTitle(),
                            habit.getDescription(),
                            habit.getChallengeDays(),
                            currentStreak,
                            habit.getFrequency());

                    pendingHabits.add(reminderDTO);
                }
            }

            log.debug(" Found {} pending habits for {}", pendingHabits.size(),
                    user.getEmail());

        } catch (Exception e) {
            log.error(" Error getting pending habits for {}: {}", user.getEmail(),
                    e.getMessage());
        }

        return pendingHabits;
    }

    // Calculate current streak using HabitProgressService logic
    private int calculateCurrentStreak(Habit habit) {
        try {
            List<HabitLog> logs = habitProgressService.getHabitLogs(habit);

            if (logs == null || logs.isEmpty()) {
                return 0;
            }

            logs.sort((log1, log2) -> log2.getLogDate().compareTo(log1.getLogDate()));

            int currentStreak = 0;
            LocalDate expectedDate = LocalDate.now();

            for (HabitLog log : logs) {
                LocalDate logDate = log.getLogDate();

                if (logDate.isAfter(LocalDate.now())) {
                    continue;
                }

                if (log.getStatus() == HabitLog.Status.DONE) {
                    if (logDate.equals(expectedDate)) {
                        currentStreak++;
                        expectedDate = expectedDate.minusDays(1);
                    } else {
                        break;
                    }
                } else if (log.getStatus() == HabitLog.Status.SKIPPED &&
                        logDate.equals(expectedDate)) {
                    break;
                } else {
                    break;
                }
            }

            return currentStreak;

        } catch (Exception e) {
            log.error(" Error calculating streak for habit {}: {}", habit.getTitle(),
                    e.getMessage());
            return 0;
        }
    }

    // Check if a habit is pending for today
    private boolean isHabitPendingForToday(Habit habit, LocalDate today) {
        try {
            if (habit.getStartDate() == null || habit.getStartDate().isAfter(today)) {
                return false;
            }

            String currentStatus = habitProgressService.getCurrentStatusForHabit(habit);
            return "PENDING".equals(currentStatus);

        } catch (Exception e) {
            log.error(" Error checking habit status for {}: {}", habit.getTitle(),
                    e.getMessage());
            return false;
        }
    }

    // Manual trigger for testing
    public void sendTestReminder(String userEmail) {
        try {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

            List<HabitReminderDTO> pendingHabits = getPendingHabitsForToday(user);

            if (!pendingHabits.isEmpty()) {
                emailService.sendDailyReminderEmail(user.getEmail(), user.getName(),
                        pendingHabits);
                log.info(" Test reminder sent to {} with {} habits", user.getEmail(),
                        pendingHabits.size());
            } else {
                log.info("ℹ No pending habits found for {}", user.getEmail());
            }

        } catch (Exception e) {
            log.error(" Test reminder failed for {}: {}", userEmail, e.getMessage());
            throw new RuntimeException("Test reminder failed: " + e.getMessage());
        }
    }
}