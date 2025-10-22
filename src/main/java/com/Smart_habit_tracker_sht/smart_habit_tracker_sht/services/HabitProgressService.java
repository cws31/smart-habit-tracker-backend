package com.Smart_habit_tracker_sht.smart_habit_tracker_sht.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.DTOs.HabitProgressResponse;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.models.Habit;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.models.HabitLog;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.models.User;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.repositories.HabitLogRepository;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.repositories.HabitRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HabitProgressService {

    private final HabitRepository habitRepository;
    private final HabitLogRepository habitLogRepository;

    // Streak calculation result helper
    private static class StreakResult {
        final int currentStreak;
        final Optional<LocalDate> breakDate;

        StreakResult(int currentStreak, Optional<LocalDate> breakDate) {
            this.currentStreak = currentStreak;
            this.breakDate = breakDate;
        }
    }

    // Calculate current active streak
    private StreakResult calculateCurrentStreak(List<HabitLog> logs) {
        if (logs == null || logs.isEmpty())
            return new StreakResult(0, Optional.empty());

        logs.sort((log1, log2) -> log2.getLogDate().compareTo(log1.getLogDate()));
        int currentStreak = 0;
        LocalDate expectedDate = logs.get(0).getLogDate().isEqual(LocalDate.now())
                ? LocalDate.now()
                : LocalDate.now().minusDays(1);

        Optional<LocalDate> breakDate = Optional.empty();
        boolean foundBreak = false;

        for (HabitLog log : logs) {
            LocalDate logDate = log.getLogDate();
            if (logDate.isAfter(LocalDate.now()))
                continue;
            if (foundBreak)
                break;

            if (log.getStatus() == HabitLog.Status.DONE) {
                if (logDate.equals(expectedDate)) {
                    currentStreak++;
                    expectedDate = expectedDate.minusDays(1);
                } else {
                    breakDate = Optional.of(expectedDate);
                    foundBreak = true;
                }
            } else if (log.getStatus() == HabitLog.Status.SKIPPED && logDate.equals(expectedDate)) {
                breakDate = Optional.of(logDate);
                foundBreak = true;
            }
        }
        return new StreakResult(currentStreak, breakDate);
    }

    // Calculate longest streak ever achieved
    public int calculateLongestStreak(List<HabitLog> logs) {
        if (logs == null || logs.isEmpty())
            return 0;

        logs.sort(Comparator.comparing(HabitLog::getLogDate));
        int longestStreak = 0;
        int currentStreak = 0;
        LocalDate previousDate = null;

        for (HabitLog log : logs) {
            LocalDate currentDate = log.getLogDate();
            if (log.getStatus() == HabitLog.Status.DONE) {
                if (previousDate == null || currentDate.equals(previousDate.plusDays(1))) {
                    currentStreak++;
                } else if (currentDate.isAfter(previousDate.plusDays(1))) {
                    currentStreak = 1;
                }
                longestStreak = Math.max(longestStreak, currentStreak);
            } else {
                currentStreak = 0;
            }
            previousDate = currentDate;
        }
        return longestStreak;
    }

    // Get daily habit history
    private List<HabitProgressResponse.DailyLogDTO> getDailyHistory(List<HabitLog> allLogs, LocalDate startDate) {
        Map<LocalDate, HabitLog> logMap = allLogs.stream()
                .collect(Collectors.toMap(HabitLog::getLogDate, log -> log));
        List<HabitProgressResponse.DailyLogDTO> history = new ArrayList<>();
        LocalDate currentDate = startDate;
        LocalDate today = LocalDate.now();

        while (!currentDate.isAfter(today)) {
            HabitLog log = logMap.get(currentDate);
            HabitLog.Status status = log != null ? log.getStatus() : HabitLog.Status.PENDING;
            history.add(new HabitProgressResponse.DailyLogDTO(currentDate, status,
                    currentDate.isAfter(today)));
            currentDate = currentDate.plusDays(1);
        }
        return history;
    }

    // Get comprehensive habit progress - UPDATED to use current challenge days
    @Transactional
    public HabitProgressResponse getHabitProgress(Long habitId) {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new RuntimeException("Habit not found"));
        List<HabitLog> logs = habitLogRepository.findByHabitOrderByLogDateAsc(habit);

        // Use the current challenge days from habit (supports dynamic updates)
        int totalDays = habit.getChallengeDays();
        StreakResult streakResult = calculateCurrentStreak(logs);
        int longestStreak = calculateLongestStreak(logs);
        int completedDays = (int) logs.stream().filter(l -> l.getStatus() == HabitLog.Status.DONE).count();
        int skippedDays = (int) logs.stream().filter(l -> l.getStatus() == HabitLog.Status.SKIPPED).count();

        // Calculate completion rate based on current challenge days
        double completionRate = totalDays > 0
                ? Math.min((streakResult.currentStreak / (double) totalDays) * 100, 100)
                : 0;

        HabitProgressResponse response = HabitProgressResponse.builder()
                .habitId(habit.getId())
                .habitTitle(habit.getTitle())
                .totalDays(totalDays)
                .completedDays(completedDays)
                .skippedDays(skippedDays)
                .completionRate(completionRate)
                .streak(streakResult.currentStreak)
                .longestStreak(longestStreak)
                .streakBreakDate(streakResult.breakDate.orElse(null))
                .dailyHistory(getDailyHistory(logs, habit.getStartDate()))
                .build();

        log.info("Progress for habit '{}': Streak={}, Completed={}, Target={}, ChallengeDays={}",
                habit.getTitle(), streakResult.currentStreak, completedDays, totalDays,
                habit.getChallengeDays());
        return response;
    }

    // Get habits by specific longest streak
    @Transactional(readOnly = true)
    public List<HabitProgressResponse> getHabitsByLongestStreak(User user, int targetStreak) {
        List<HabitProgressResponse> result = new ArrayList<>();
        for (Habit habit : habitRepository.findByUser(user)) {
            List<HabitLog> logs = habitLogRepository.findByHabitOrderByLogDateAsc(habit);
            if (calculateLongestStreak(logs) == targetStreak) {
                result.add(getHabitProgress(habit.getId()));
            }
        }
        log.info("Found {} habits with longest streak of {} for user: {}", result.size(), targetStreak,
                user.getEmail());
        return result;
    }

    // Force progress recalculation
    public void calculateHabitProgress(Long habitId) {
        log.info("Forcing progress recalculation for habit ID: {}", habitId);
        getHabitProgress(habitId);
    }

    public List<HabitLog> getHabitLogs(Habit habit) {
        return habitLogRepository.findByHabitOrderByLogDateAsc(habit);
    }

    @Transactional(readOnly = true)
    public String calculateOverallPerformance(User user) {
        List<Habit> habits = habitRepository.findByUser(user);
        if (habits.isEmpty()) {
            return "Overall performance is 0%";
        }

        double totalCompletion = 0.0;

        for (Habit habit : habits) {
            List<HabitLog> logs = habitLogRepository.findByHabitOrderByLogDateAsc(habit);
            int totalDays = habit.getChallengeDays();
            if (totalDays == 0)
                continue;

            long doneDays = logs.stream()
                    .filter(l -> l.getStatus() == HabitLog.Status.DONE)
                    .count();

            double completionRate = (doneDays / (double) totalDays) * 100;
            totalCompletion += completionRate;
        }

        double overallPerformance = totalCompletion / habits.size();

        return "Overall performance is " + Math.round(overallPerformance) + "%";
    }

    public String getCurrentStatusForHabit(Habit habit) {
        try {
            List<HabitLog> logs = habitLogRepository.findByHabitOrderByLogDateAsc(habit);
            LocalDate today = LocalDate.now();

            // Check if habit has started
            if (habit.getStartDate() == null || habit.getStartDate().isAfter(today)) {
                return "NOT_STARTED";
            }

            // Check if today's log exists and its status
            Optional<HabitLog> todayLog = logs.stream()
                    .filter(log -> log.getLogDate().equals(today))
                    .findFirst();

            if (todayLog.isPresent()) {
                HabitLog.Status status = todayLog.get().getStatus();
                switch (status) {
                    case DONE:
                        return "COMPLETED";
                    case SKIPPED:
                        return "SKIPPED";
                    case PENDING:
                        return "PENDING";
                }
            }

            // If no log exists for today, check if we should create one
            // For daily habits, if it's after start date and before/on today, it's pending
            if (!today.isBefore(habit.getStartDate())) {
                return "PENDING";
            }

            return "NOT_APPLICABLE";

        } catch (Exception e) {
            log.error("❌ Error getting current status for habit {}: {}", habit.getTitle(), e.getMessage());
            return "ERROR";
        }
    }
}
