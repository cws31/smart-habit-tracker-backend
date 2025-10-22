package com.Smart_habit_tracker_sht.smart_habit_tracker_sht.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.DTOs.HabitRequest;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.DTOs.HabitResponse;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.DTOs.SetChallengeRequest;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.models.ChallengeHistory;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.models.Habit;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.models.HabitLog;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.models.User;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.repositories.ChallengeHistoryRepository;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.repositories.HabitLogRepository;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.repositories.HabitRepository;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.repositories.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class HabitService {

    private final HabitRepository habitRepository;
    private final UserRepository userRepository;
    private final HabitLogRepository habitLogRepository;
    private final ChallengeHistoryRepository challengeHistoryRepository;
    private final emailService emailService;

    // create habit
    public HabitResponse createHabit(HabitRequest request) {
        User user = getLoggedInUser();
        log.info(" Creating new habit '{}' for user: {}", request.getTitle(), user.getEmail());
        LocalDate today = LocalDate.now();
        if (request.getStartDate() != null && request.getStartDate().isBefore(today)) {
            throw new RuntimeException("Start date cannot be in the past. Please select today or a future date.");
        }

        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : today;

        Habit habit = Habit.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .frequency(request.getFrequency())
                .startDate(startDate)
                .challengeDays(request.getChallengeDays() != null ? request.getChallengeDays() : 21)
                .user(user)
                .build();

        Habit savedHabit = habitRepository.save(habit);

        saveChallengeChangeToHistory(savedHabit, 0, savedHabit.getChallengeDays(), 0, "STARTED");

        log.info(" Habit created successfully: {} (ID: {}) with start date: {}",
                savedHabit.getTitle(), savedHabit.getId(), savedHabit.getStartDate());

        return mapToDtoWithCurrentStatus(savedHabit);
    }

    public List<HabitResponse> getHabitsForLoggedInUser() {
        User user = getLoggedInUser();
        List<Habit> habits = habitRepository.findByUser(user);
        log.debug("📋 Retrieving {} habits for user: {}", habits.size(), user.getEmail());

        return habits.stream()
                .map(this::mapToDtoWithCurrentStatus)
                .collect(Collectors.toList());
    }

    public HabitResponse updateHabit(Long habitId, HabitRequest request) {
        User user = getLoggedInUser();
        log.info("✏️ Updating habit ID: {} for user: {}", habitId, user.getEmail());

        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new RuntimeException("Habit not found"));

        if (!habit.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("This habit does not belong to the logged-in user");
        }

        LocalDate today = LocalDate.now();
        if (request.getStartDate() != null && request.getStartDate().isBefore(today)) {
            throw new RuntimeException("Start date cannot be in the past. Please select today or a future date.");
        }

        habit.setTitle(request.getTitle());
        habit.setDescription(request.getDescription());
        habit.setFrequency(request.getFrequency());

        if (request.getStartDate() != null) {
            habit.setStartDate(request.getStartDate());
        }

        Habit updated = habitRepository.save(habit);
        log.info(" Habit updated successfully: {} (ID: {})", updated.getTitle(), updated.getId());

        return mapToDtoWithCurrentStatus(updated);
    }

    // Set custom challenge days for a habit (initial setup)
    public HabitResponse setCustomChallenge(SetChallengeRequest request) {
        User user = getLoggedInUser();
        Habit habit = habitRepository.findById(request.getHabitId())
                .orElseThrow(() -> new RuntimeException("Habit not found"));

        if (!habit.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("This habit does not belong to the logged-in user");
        }

        if (request.getChallengeDays() < 1) {
            throw new RuntimeException("Challenge days must be at least 1");
        }

        int previousChallengeDays = habit.getChallengeDays();
        int currentStreak = calculateCurrentStreak(habit);

        boolean isActualChange = previousChallengeDays != request.getChallengeDays();

        if (isActualChange) {
            saveChallengeChangeToHistory(habit, previousChallengeDays, request.getChallengeDays(), currentStreak,
                    "UPDATED");
            log.info(" Challenge updated: {} -> {} days for '{}'",
                    previousChallengeDays, request.getChallengeDays(), habit.getTitle());
        }

        habit.setChallengeDays(request.getChallengeDays());
        Habit updated = habitRepository.save(habit);

        return mapToDtoWithCurrentStatus(updated);
    }

    // Enhanced challenge update with proper history tracking
    public HabitResponse updateChallenge(SetChallengeRequest request) {
        User user = getLoggedInUser();
        Habit habit = habitRepository.findById(request.getHabitId())
                .orElseThrow(() -> new RuntimeException("Habit not found"));

        if (!habit.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("This habit does not belong to the logged-in user");
        }

        int currentStreak = calculateCurrentStreak(habit);
        int previousChallengeDays = habit.getChallengeDays();
        int newChallengeDays = request.getChallengeDays();

        if (newChallengeDays < 1) {
            throw new RuntimeException("Challenge days must be at least 1");
        }

        boolean hasChanged = newChallengeDays != previousChallengeDays;

        if (hasChanged) {
            saveChallengeChangeToHistory(habit, previousChallengeDays, newChallengeDays, currentStreak, "UPDATED");
            log.info(" Challenge updated: {} -> {} days for '{}'. Current streak: {} days",
                    previousChallengeDays, newChallengeDays, habit.getTitle(), currentStreak);
        }

        if (newChallengeDays < previousChallengeDays && currentStreak >= newChallengeDays) {
            saveChallengeChangeToHistory(habit, newChallengeDays, currentStreak, currentStreak, "COMPLETED");
            log.info(" Challenge completed due to decrease: {} days target, {} days achieved for '{}'",
                    newChallengeDays, currentStreak, habit.getTitle());
        }

        if (currentStreak >= previousChallengeDays && previousChallengeDays > 0 && hasChanged) {
            saveChallengeChangeToHistory(habit, previousChallengeDays, currentStreak, currentStreak, "COMPLETED");
            log.info(" Challenge naturally completed: {} days target, {} days achieved for '{}'",
                    previousChallengeDays, currentStreak, habit.getTitle());
        }

        if (hasChanged) {
            habit.setChallengeDays(newChallengeDays);
        }

        Habit updated = habitRepository.save(habit);
        return mapToDtoWithCurrentStatus(updated);
    }

    // Get challenge history for a habit - FROM DATABASE
    public List<Map<String, Object>> getChallengeHistory(Long habitId) {
        User user = getLoggedInUser();
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new RuntimeException("Habit not found"));

        if (!habit.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("This habit does not belong to the logged-in user");
        }

        List<ChallengeHistory> historyEntries = challengeHistoryRepository.findByHabitOrderByChangedAtDesc(habit);

        List<Map<String, Object>> history = historyEntries.stream()
                .map(this::mapChallengeHistoryToResponse)
                .collect(Collectors.toList());

        log.info("📋 Retrieved {} challenge history entries for habit '{}' from database",
                history.size(), habit.getTitle());
        return history;
    }

    // history storage method that saves to database
    private void saveChallengeChangeToHistory(Habit habit, int previousDays, int newDays, int currentStreak,
            String type) {
        try {
            if (!isMeaningfulHistoryEntry(previousDays, newDays, type)) {
                return;
            }

            ChallengeHistory historyEntry = ChallengeHistory.builder()
                    .habit(habit)
                    .previousDays(previousDays)
                    .newDays(newDays)
                    .currentStreak(currentStreak)
                    .changedAt(LocalDateTime.now())
                    .type(type)
                    .build();

            challengeHistoryRepository.save(historyEntry);

            log.info(" Challenge history saved to database: {} ({} -> {}) for '{}' at {}",
                    type, previousDays, newDays, habit.getTitle(), LocalDateTime.now());
        } catch (Exception e) {
            log.error(" Failed to save challenge history to database: {}", e.getMessage());
        }
    }

    // Helper method to determine if a history entry is meaningful
    private boolean isMeaningfulHistoryEntry(int previousDays, int newDays, String type) {
        if ("UPDATED".equals(type) && previousDays == newDays) {
            return false;
        }
        return "COMPLETED".equals(type) || "STARTED".equals(type) || "UPDATED".equals(type);
    }

    // Track habit completion for challenge completion detection
    public void trackHabitCompletion(Long habitId) {
        try {
            Habit habit = habitRepository.findById(habitId)
                    .orElseThrow(() -> new RuntimeException("Habit not found"));

            int currentStreak = calculateCurrentStreak(habit);
            int challengeDays = habit.getChallengeDays();

            if (currentStreak >= challengeDays && challengeDays > 0) {
                if (!hasRecentCompletion(habit.getId(), challengeDays)) {
                    saveChallengeChangeToHistory(habit, challengeDays, currentStreak, currentStreak, "COMPLETED");
                    log.info("🎉 Challenge completion tracked: {}/{} days for '{}'",
                            currentStreak, challengeDays, habit.getTitle());
                }
            }
        } catch (Exception e) {
            log.error(" Failed to track habit completion: {}", e.getMessage());
        }
    }

    // Helper to avoid duplicate completion entries - USING DATABASE
    private boolean hasRecentCompletion(Long habitId, int challengeDays) {
        LocalDate today = LocalDate.now();
        List<ChallengeHistory> recentCompletions = challengeHistoryRepository.findRecentCompletions(
                habitId, challengeDays, today);

        return !recentCompletions.isEmpty();
    }

    public void deleteHabit(Long habitId) {
        User user = getLoggedInUser();
        log.info("🗑️ Deleting habit ID: {} for user: {}", habitId, user.getEmail());

        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new RuntimeException("Habit not found"));

        if (!habit.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("This habit does not belong to the logged-in user");
        }

        challengeHistoryRepository.deleteByHabitId(habitId);

        habitRepository.delete(habit);
        log.info(" Habit deleted successfully: {} (ID: {})", habit.getTitle(), habitId);
    }

    // Method to mark habit as done/skipped
    public HabitResponse markHabit(Long habitId, String status) {
        User user = getLoggedInUser();
        log.info(" Marking habit ID: {} as {} for user: {}", habitId, status, user.getEmail());

        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new RuntimeException("Habit not found"));

        if (!habit.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("This habit does not belong to the logged-in user");
        }

        List<HabitLog> todayLogs = habitLogRepository.findByHabitAndLogDate(habit, LocalDate.now());
        if (!todayLogs.isEmpty()) {
            throw new RuntimeException("Habit already marked for today");
        }

        HabitLog.Status logStatus = HabitLog.Status.valueOf(status.toUpperCase());
        HabitLog habitLog = HabitLog.builder()
                .habit(habit)
                .logDate(LocalDate.now())
                .status(logStatus)
                .build();

        habitLogRepository.save(habitLog);
        log.info(" Habit '{}' marked as {} on {}", habit.getTitle(), status, LocalDate.now());
        if (logStatus == HabitLog.Status.DONE) {
            checkAndNotifyChallengeCompletion(habit, user);

            trackHabitCompletion(habitId);
        }

        return mapToDtoWithCurrentStatus(habit);
    }

    // Method to check if challenge is completed and send email
    private void checkAndNotifyChallengeCompletion(Habit habit, User user) {
        try {
            log.debug(" Checking challenge completion for habit: {}", habit.getTitle());

            int currentStreak = calculateCurrentStreak(habit);
            int challengeDays = habit.getChallengeDays();

            log.debug(" Current streak: {}/{} for habit '{}'", currentStreak, challengeDays, habit.getTitle());

            if (currentStreak >= challengeDays && challengeDays > 0) {
                log.info(" Challenge completed! Habit: {}, Streak: {}/{}",
                        habit.getTitle(), currentStreak, challengeDays);

                emailService.sendChallengeCompletedEmail(
                        user.getEmail(),
                        user.getName(),
                        habit.getTitle(),
                        currentStreak,
                        challengeDays);

                log.info(" Challenge completion email sent to: {}", user.getEmail());
            }
        } catch (Exception e) {
            log.error(" Failed to check challenge completion for habit {}: {}",
                    habit.getTitle(), e.getMessage());
        }
    }

    private int calculateCurrentStreak(Habit habit) {
        List<HabitLog> logs = habitLogRepository.findByHabitOrderByLogDateDesc(habit);

        if (logs.isEmpty()) {
            return 0;
        }

        int streak = 0;
        LocalDate currentDate = LocalDate.now();

        if (currentDate.isBefore(habit.getStartDate())) {
            return 0;
        }

        for (HabitLog log : logs) {

            if (log.getLogDate().isBefore(habit.getStartDate())) {
                continue;
            }

            if (log.getStatus() == HabitLog.Status.DONE &&
                    log.getLogDate().equals(currentDate.minusDays(streak))) {
                streak++;
            } else {
                break;
            }
        }

        return streak;
    }

    // Method to get habit progress
    public Map<String, Object> getHabitProgress(Long habitId) {
        User user = getLoggedInUser();
        log.debug(" Getting progress for habit ID: {} for user: {}", habitId, user.getEmail());

        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new RuntimeException("Habit not found"));

        if (!habit.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("This habit does not belong to the logged-in user");
        }

        List<HabitLog> allLogs = habitLogRepository.findByHabit(habit);

        List<HabitLog> validLogs = allLogs.stream()
                .filter(log -> !log.getLogDate().isBefore(habit.getStartDate()))
                .collect(Collectors.toList());

        long totalDays = validLogs.stream().count();
        long completedDays = validLogs.stream()
                .filter(log -> log.getStatus() == HabitLog.Status.DONE)
                .count();

        double completionRate = totalDays > 0 ? (completedDays * 100.0) / totalDays : 0.0;
        int currentStreak = calculateCurrentStreak(habit);
        int longestStreak = calculateLongestStreak(habit);

        LocalDate streakBreakDate = findStreakBreakDate(habit);

        Map<String, Object> progress = new HashMap<>();
        progress.put("totalDays", totalDays);
        progress.put("completedDays", completedDays);
        progress.put("completionRate", Math.round(completionRate * 10.0) / 10.0);
        progress.put("streak", currentStreak);
        progress.put("longestStreak", longestStreak);
        progress.put("streakBreakDate", streakBreakDate);
        progress.put("challengeDays", habit.getChallengeDays());

        progress.put("startDate", habit.getStartDate());

        log.debug("📊 Progress calculated for habit '{}': {}/{} days, {}% completion, {} day streak, start date: {}",
                habit.getTitle(), completedDays, totalDays, completionRate, currentStreak, habit.getStartDate());

        return progress;
    }

    // Update longest streak calculation to respect start date
    private int calculateLongestStreak(Habit habit) {
        List<HabitLog> logs = habitLogRepository.findByHabitOrderByLogDateAsc(habit);

        if (logs.isEmpty()) {
            return 0;
        }

        int longestStreak = 0;
        int currentStreak = 0;
        LocalDate expectedDate = habit.getStartDate();

        for (HabitLog log : logs) {
            if (log.getLogDate().isBefore(habit.getStartDate())) {
                continue;
            }

            if (log.getStatus() == HabitLog.Status.DONE) {
                if (log.getLogDate().equals(expectedDate)) {
                    currentStreak++;
                    expectedDate = expectedDate.plusDays(1);
                } else {
                    longestStreak = Math.max(longestStreak, currentStreak);
                    currentStreak = 1;
                    expectedDate = log.getLogDate().plusDays(1);
                }
            } else {
                longestStreak = Math.max(longestStreak, currentStreak);
                currentStreak = 0;
                expectedDate = log.getLogDate().plusDays(1);
            }
        }

        return Math.max(longestStreak, currentStreak);
    }

    private LocalDate findStreakBreakDate(Habit habit) {
        List<HabitLog> logs = habitLogRepository.findByHabitOrderByLogDateDesc(habit);

        if (logs.isEmpty()) {
            return null;
        }

        LocalDate currentDate = LocalDate.now();
        LocalDate lastLogDate = logs.get(0).getLogDate();
        if (!lastLogDate.equals(currentDate) &&
                (logs.get(0).getStatus() != HabitLog.Status.DONE ||
                        calculateCurrentStreak(habit) == 0)) {
            return lastLogDate.plusDays(1);
        }

        return null;
    }

    // Helper method to map ChallengeHistory entity to response map
    private Map<String, Object> mapChallengeHistoryToResponse(ChallengeHistory history) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", history.getId());
        response.put("previousDays", history.getPreviousDays());
        response.put("newDays", history.getNewDays());
        response.put("currentStreak", history.getCurrentStreak());
        response.put("changedAt", history.getChangedAt());
        response.put("habitTitle", history.getHabit().getTitle());
        response.put("habitId", history.getHabit().getId());
        response.put("type", history.getType());
        return response;
    }

    private User getLoggedInUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private HabitResponse mapToDto(Habit habit) {
        HabitResponse response = new HabitResponse();
        response.setId(habit.getId());
        response.setTitle(habit.getTitle());
        response.setDescription(habit.getDescription());
        response.setFrequency(habit.getFrequency());
        response.setStartDate(habit.getStartDate());
        response.setChallengeDays(habit.getChallengeDays());
        response.setUserId(habit.getUser().getId());
        return response;
    }

    private HabitResponse mapToDtoWithCurrentStatus(Habit habit) {
        HabitResponse response = mapToDto(habit);

        String currentStatus = getCurrentStatusForHabit(habit);
        response.setCurrentStatus(currentStatus);

        return response;
    }

    private String getCurrentStatusForHabit(Habit habit) {
        try {
            List<HabitLog> todayLogs = habitLogRepository.findByHabitAndLogDate(habit, LocalDate.now());

            if (todayLogs.isEmpty()) {
                return "PENDING";
            } else {
                HabitLog.Status status = todayLogs.get(0).getStatus();
                return status.name();
            }
        } catch (Exception e) {
            return "PENDING";
        }
    }
}